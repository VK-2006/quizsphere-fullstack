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

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final CategoryRepository categoryRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuizAttemptRepository attemptRepository;
    private final CurrentUserService currentUserService;
    private final QuizService quizService;

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BadRequestException("Category name already exists");
        }
        Category category = categoryRepository.save(Category.builder()
                .name(request.name().trim()).description(request.description()).build());
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setName(request.name().trim());
        category.setDescription(request.description());
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) throw new ResourceNotFoundException("Category not found");
        if (quizRepository.existsByCategoryId(id)) {
            throw new BadRequestException("This category contains quizzes. Move or delete them first.");
        }
        categoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<QuizResponse> allQuizzes() {
        return quizRepository.findAll().stream().map(quizService::quizResponse).toList();
    }

    @Transactional
    public QuizResponse createQuiz(QuizRequest request, Authentication auth) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        User creator = currentUserService.require(auth);
        Quiz quiz = quizRepository.save(Quiz.builder()
                .title(request.title().trim()).description(request.description()).difficulty(request.difficulty())
                .durationMinutes(request.durationMinutes()).passPercentage(request.passPercentage())
                .published(request.published()).category(category).createdBy(creator).build());
        return quizService.quizResponse(quiz);
    }

    @Transactional
    public QuizResponse updateQuiz(Long id, QuizRequest request) {
        Quiz quiz = quizRepository.findWithQuestionsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        quiz.setTitle(request.title().trim());
        quiz.setDescription(request.description());
        quiz.setDifficulty(request.difficulty());
        quiz.setDurationMinutes(request.durationMinutes());
        quiz.setPassPercentage(request.passPercentage());
        quiz.setPublished(request.published());
        quiz.setCategory(category);
        return quizService.quizResponse(quiz);
    }

    @Transactional
    public void deleteQuiz(Long id) {
        if (!quizRepository.existsById(id)) throw new ResourceNotFoundException("Quiz not found");
        if (attemptRepository.existsByQuizId(id)) {
            throw new BadRequestException("This quiz has attempt history. Unpublish it instead of deleting it.");
        }
        quizRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AdminQuestionResponse> questions(Long quizId) {
        return questionRepository.findByQuizIdOrderByIdAsc(quizId).stream().map(this::questionResponse).toList();
    }

    @Transactional
    public AdminQuestionResponse createQuestion(Long quizId, QuestionRequest request) {
        validateCorrectOption(request);
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        Question question = Question.builder().quiz(quiz).questionText(request.questionText().trim())
                .explanation(request.explanation()).marks(request.marks()).build();
        request.options().forEach(o -> question.getOptions().add(QuestionOption.builder()
                .question(question).optionText(o.optionText().trim()).correct(o.correct()).build()));
        return questionResponse(questionRepository.save(question));
    }

    @Transactional
    public AdminQuestionResponse updateQuestion(Long questionId, QuestionRequest request) {
        validateCorrectOption(request);
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        question.setQuestionText(request.questionText().trim());
        question.setExplanation(request.explanation());
        question.setMarks(request.marks());
        question.getOptions().clear();
        request.options().forEach(o -> question.getOptions().add(QuestionOption.builder()
                .question(question).optionText(o.optionText().trim()).correct(o.correct()).build()));
        return questionResponse(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) throw new ResourceNotFoundException("Question not found");
        questionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> users() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole().name(), u.isEnabled()))
                .toList();
    }

    @Transactional
    public UserResponse toggleUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN) throw new BadRequestException("Admin account cannot be disabled here");
        user.setEnabled(!user.isEnabled());
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(), user.isEnabled());
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboard() {
        return new DashboardResponse(userRepository.count(), categoryRepository.count(), quizRepository.count(), attemptRepository.count());
    }

    private void validateCorrectOption(QuestionRequest request) {
        long correct = request.options().stream().filter(OptionRequest::correct).count();
        if (correct != 1) throw new BadRequestException("Exactly one option must be marked correct");
    }

    private AdminQuestionResponse questionResponse(Question q) {
        return new AdminQuestionResponse(q.getId(), q.getQuestionText(), q.getExplanation(), q.getMarks(),
                q.getOptions().stream().map(o -> new AdminQuestionResponse.AdminOptionResponse(
                        o.getId(), o.getOptionText(), o.isCorrect())).toList());
    }
}
