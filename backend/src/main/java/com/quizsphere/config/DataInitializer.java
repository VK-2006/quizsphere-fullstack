package com.quizsphere.config;

import com.quizsphere.entity.*;
import com.quizsphere.repository.CategoryRepository;
import com.quizsphere.repository.QuizRepository;
import com.quizsphere.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final QuizRepository quizRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${app.seed.admin-email:}")
    private String adminEmail;

    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    @Value("${app.seed.admin-name:QuizSphere Admin}")
    private String adminName;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.length() < 8) {
            log.warn("Sample-data seeding is enabled, but ADMIN_EMAIL/ADMIN_PASSWORD are missing or invalid. Skipping seed data.");
            return;
        }

        String normalizedEmail = adminEmail.trim().toLowerCase();
        User admin = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseGet(() -> userRepository.save(User.builder()
                        .fullName(adminName == null || adminName.isBlank() ? "QuizSphere Admin" : adminName.trim())
                        .email(normalizedEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .authProvider(AuthProvider.LOCAL)
                        .role(Role.ADMIN)
                        .enabled(true)
                        .build()));

        if (admin.getRole() != Role.ADMIN) {
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        if (quizRepository.count() > 0) {
            return;
        }

        Category category = categoryRepository.save(Category.builder()
                .name("Programming")
                .description("Test your programming fundamentals")
                .build());

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
        log.info("QuizSphere sample admin and Java Fundamentals quiz were created.");
    }

    private void addQuestion(Quiz quiz, String text, String explanation, String[] options, int correctIndex) {
        Question question = Question.builder()
                .quiz(quiz)
                .questionText(text)
                .explanation(explanation)
                .marks(1)
                .build();
        for (int i = 0; i < options.length; i++) {
            question.getOptions().add(QuestionOption.builder()
                    .question(question)
                    .optionText(options[i])
                    .correct(i == correctIndex)
                    .build());
        }
        quiz.getQuestions().add(question);
    }
}
