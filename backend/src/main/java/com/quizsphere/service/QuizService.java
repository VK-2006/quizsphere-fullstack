package com.quizsphere.service;

import com.quizsphere.dto.*;
import com.quizsphere.entity.*;
import com.quizsphere.exception.BadRequestException;
import com.quizsphere.exception.ResourceNotFoundException;
import com.quizsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final CategoryRepository categoryRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> publishedQuizzes() {
        return quizRepository.findByPublishedTrueOrderByCreatedAtDesc().stream().map(this::quizResponse).toList();
    }

    @Transactional(readOnly = true)
    public QuizResponse publicQuiz(Long id) {
        Quiz quiz = quizRepository.findWithQuestionsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        if (!quiz.isPublished()) throw new ResourceNotFoundException("Quiz not found");
        return quizResponse(quiz);
    }

    @Transactional
    public QuizPlayResponse start(Long quizId, Authentication authentication) {
        User user = currentUserService.require(authentication);
        Quiz quiz = quizRepository.findWithQuestionsById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        if (!quiz.isPublished()) throw new BadRequestException("This quiz is not published");
        if (quiz.getQuestions().isEmpty()) throw new BadRequestException("This quiz has no questions");

        QuizAttempt attempt = attemptRepository.save(QuizAttempt.builder()
                .user(user).quiz(quiz).startedAt(Instant.now()).status(AttemptStatus.IN_PROGRESS).build());

        List<QuizPlayResponse.PlayQuestion> questions = quiz.getQuestions().stream()
                .map(q -> new QuizPlayResponse.PlayQuestion(q.getId(), q.getQuestionText(), q.getMarks(),
                        q.getOptions().stream()
                                .map(o -> new QuizPlayResponse.PlayOption(o.getId(), o.getOptionText()))
                                .toList()))
                .toList();

        return new QuizPlayResponse(attempt.getId(), quiz.getId(), quiz.getTitle(), quiz.getDescription(),
                quiz.getDurationMinutes(), attempt.getStartedAt(), questions);
    }

    @Transactional
    public AttemptResultResponse submit(Long attemptId, SubmitAttemptRequest request, Authentication authentication) {
        User user = currentUserService.require(authentication);
        QuizAttempt attempt = attemptRepository.findDetailedById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
        if (!attempt.getUser().getId().equals(user.getId())) throw new BadRequestException("This attempt belongs to another user");
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) throw new BadRequestException("This attempt was already completed");

        Quiz quiz = attempt.getQuiz();
        boolean expired = Instant.now().isAfter(attempt.getStartedAt().plus(Duration.ofMinutes(quiz.getDurationMinutes() + 1L)));

        Map<Long, Long> submitted = new HashMap<>();
        if (request.answers() != null) {
            request.answers().forEach(answer -> submitted.put(answer.questionId(), answer.selectedOptionId()));
        }

        int score = 0;
        int totalMarks = 0;
        for (Question question : quiz.getQuestions()) {
            totalMarks += question.getMarks();
            Long selectedId = submitted.get(question.getId());
            QuestionOption selected = selectedId == null ? null : optionRepository.findById(selectedId).orElse(null);
            if (selected != null && !selected.getQuestion().getId().equals(question.getId())) {
                selected = null;
            }
            boolean correct = selected != null && selected.isCorrect();
            int awarded = correct ? question.getMarks() : 0;
            score += awarded;
            attempt.getAnswers().add(AttemptAnswer.builder()
                    .attempt(attempt).question(question).selectedOption(selected)
                    .correct(correct).marksAwarded(awarded).build());
        }

        double percentage = totalMarks == 0 ? 0 : Math.round((score * 10000.0 / totalMarks)) / 100.0;
        attempt.setScore(score);
        attempt.setTotalMarks(totalMarks);
        attempt.setPercentage(percentage);
        attempt.setPassed(percentage >= quiz.getPassPercentage());
        attempt.setSubmittedAt(Instant.now());
        attempt.setStatus(expired ? AttemptStatus.EXPIRED : AttemptStatus.SUBMITTED);
        attemptRepository.save(attempt);
        return result(attempt);
    }

    @Transactional(readOnly = true)
    public AttemptResultResponse result(Long attemptId, Authentication authentication) {
        QuizAttempt attempt = ownedAttempt(attemptId, authentication);
        return result(attempt);
    }

    @Transactional(readOnly = true)
    public List<AttemptHistoryResponse> history(Authentication authentication) {
        User user = currentUserService.require(authentication);
        return attemptRepository.findByUserIdOrderByStartedAtDesc(user.getId()).stream()
                .map(a -> new AttemptHistoryResponse(a.getId(), a.getQuiz().getId(), a.getQuiz().getTitle(),
                        a.getQuiz().getCategory().getName(), a.getScore(), a.getTotalMarks(), a.getPercentage(),
                        a.isPassed(), a.getStatus().name(), a.getStartedAt(), a.getSubmittedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewResponse review(Long attemptId, Authentication authentication) {
        QuizAttempt attempt = ownedAttempt(attemptId, authentication);
        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) throw new BadRequestException("Submit the quiz before reviewing answers");

        List<ReviewResponse.ReviewItem> items = attempt.getAnswers().stream().map(answer -> {
            String correctText = answer.getQuestion().getOptions().stream()
                    .filter(QuestionOption::isCorrect).findFirst().map(QuestionOption::getOptionText).orElse("Not configured");
            String selectedText = answer.getSelectedOption() == null ? "Not answered" : answer.getSelectedOption().getOptionText();
            return new ReviewResponse.ReviewItem(answer.getQuestion().getId(), answer.getQuestion().getQuestionText(),
                    selectedText, correctText, answer.isCorrect(), answer.getMarksAwarded(),
                    answer.getQuestion().getMarks(), answer.getQuestion().getExplanation());
        }).toList();
        return new ReviewResponse(attempt.getId(), attempt.getQuiz().getTitle(), items);
    }

    private QuizAttempt ownedAttempt(Long attemptId, Authentication authentication) {
        User user = currentUserService.require(authentication);
        QuizAttempt attempt = attemptRepository.findDetailedById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));
        if (!attempt.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new BadRequestException("This attempt belongs to another user");
        }
        return attempt;
    }

    public QuizResponse quizResponse(Quiz quiz) {
        return new QuizResponse(quiz.getId(), quiz.getTitle(), quiz.getDescription(), quiz.getDifficulty().name(),
                quiz.getDurationMinutes(), quiz.getPassPercentage(), quiz.isPublished(), quiz.getCategory().getId(),
                quiz.getCategory().getName(), quiz.getQuestions() == null ? 0 : quiz.getQuestions().size());
    }

    private AttemptResultResponse result(QuizAttempt attempt) {
        return new AttemptResultResponse(attempt.getId(), attempt.getQuiz().getId(), attempt.getQuiz().getTitle(),
                attempt.getScore(), attempt.getTotalMarks(), attempt.getPercentage(), attempt.isPassed(),
                attempt.getStatus().name(), attempt.getStartedAt(), attempt.getSubmittedAt());
    }
}
