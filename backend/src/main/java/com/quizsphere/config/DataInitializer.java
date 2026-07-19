package com.quizsphere.config;

import com.quizsphere.entity.*;
import com.quizsphere.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final QuizRepository quizRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        User admin = userRepository.findByEmailIgnoreCase("admin@quizsphere.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .fullName("QuizSphere Admin")
                        .email("admin@quizsphere.com")
                        .password(passwordEncoder.encode("Admin@123"))
                        .authProvider(AuthProvider.LOCAL)
                        .role(Role.ADMIN).enabled(true).build()));

        if (quizRepository.count() > 0) return;

        Category category = categoryRepository.save(Category.builder()
                .name("Programming").description("Test your programming fundamentals").build());

        Quiz quiz = Quiz.builder()
                .title("Java Fundamentals")
                .description("A starter quiz covering core Java concepts.")
                .difficulty(Difficulty.EASY)
                .durationMinutes(10)
                .passPercentage(60)
                .published(true)
                .category(category)
                .createdBy(admin)
                .build();

        addQuestion(quiz, "Which keyword is used to inherit a class in Java?",
                "Java classes use the extends keyword for class inheritance.",
                new String[]{"implements", "extends", "inherits", "super"}, 1);
        addQuestion(quiz, "Which collection does not allow duplicate elements?",
                "A Set models a collection of unique elements.",
                new String[]{"List", "Queue", "Set", "ArrayList"}, 2);
        addQuestion(quiz, "What is the entry point of a standard Java application?",
                "The JVM invokes public static void main(String[] args).",
                new String[]{"start()", "run()", "main()", "init()"}, 2);

        quizRepository.save(quiz);
    }

    private void addQuestion(Quiz quiz, String text, String explanation, String[] options, int correctIndex) {
        Question question = Question.builder().quiz(quiz).questionText(text).explanation(explanation).marks(1).build();
        for (int i = 0; i < options.length; i++) {
            question.getOptions().add(QuestionOption.builder().question(question)
                    .optionText(options[i]).correct(i == correctIndex).build());
        }
        quiz.getQuestions().add(question);
    }
}
