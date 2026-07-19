package com.quizsphere.controller;

import com.quizsphere.dto.*;
import com.quizsphere.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() { return adminService.dashboard(); }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest request) {
        return adminService.createCategory(request);
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return adminService.updateCategory(id, request);
    }

    @DeleteMapping("/categories/{id}")
    public MessageResponse deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id); return new MessageResponse("Category deleted");
    }

    @GetMapping("/quizzes")
    public List<QuizResponse> quizzes() { return adminService.allQuizzes(); }

    @PostMapping("/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse createQuiz(@Valid @RequestBody QuizRequest request, Authentication authentication) {
        return adminService.createQuiz(request, authentication);
    }

    @PutMapping("/quizzes/{id}")
    public QuizResponse updateQuiz(@PathVariable Long id, @Valid @RequestBody QuizRequest request) {
        return adminService.updateQuiz(id, request);
    }

    @DeleteMapping("/quizzes/{id}")
    public MessageResponse deleteQuiz(@PathVariable Long id) {
        adminService.deleteQuiz(id); return new MessageResponse("Quiz deleted");
    }

    @GetMapping("/quizzes/{quizId}/questions")
    public List<AdminQuestionResponse> questions(@PathVariable Long quizId) {
        return adminService.questions(quizId);
    }

    @PostMapping("/quizzes/{quizId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminQuestionResponse createQuestion(@PathVariable Long quizId, @Valid @RequestBody QuestionRequest request) {
        return adminService.createQuestion(quizId, request);
    }

    @PutMapping("/questions/{id}")
    public AdminQuestionResponse updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return adminService.updateQuestion(id, request);
    }

    @DeleteMapping("/questions/{id}")
    public MessageResponse deleteQuestion(@PathVariable Long id) {
        adminService.deleteQuestion(id); return new MessageResponse("Question deleted");
    }

    @GetMapping("/users")
    public List<UserResponse> users() { return adminService.users(); }

    @PatchMapping("/users/{id}/toggle")
    public UserResponse toggleUser(@PathVariable Long id) { return adminService.toggleUser(id); }
}
