package com.quizsphere.controller;

import com.quizsphere.dto.*;
import com.quizsphere.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "UP"); }

    @GetMapping("/categories")
    public List<CategoryResponse> categories() { return quizService.categories(); }

    @GetMapping("/quizzes")
    public List<QuizResponse> quizzes() { return quizService.publishedQuizzes(); }

    @GetMapping("/quizzes/{id}")
    public QuizResponse quiz(@PathVariable Long id) { return quizService.publicQuiz(id); }

    @PostMapping("/quizzes/{id}/start")
    public QuizPlayResponse start(@PathVariable Long id, Authentication authentication) {
        return quizService.start(id, authentication);
    }

    @PostMapping("/attempts/{id}/submit")
    public AttemptResultResponse submit(@PathVariable Long id, @Valid @RequestBody SubmitAttemptRequest request,
                                        Authentication authentication) {
        return quizService.submit(id, request, authentication);
    }

    @GetMapping("/attempts/{id}/result")
    public AttemptResultResponse result(@PathVariable Long id, Authentication authentication) {
        return quizService.result(id, authentication);
    }

    @GetMapping("/attempts/{id}/review")
    public ReviewResponse review(@PathVariable Long id, Authentication authentication) {
        return quizService.review(id, authentication);
    }

    @GetMapping("/users/me/attempts")
    public List<AttemptHistoryResponse> history(Authentication authentication) {
        return quizService.history(authentication);
    }
}
