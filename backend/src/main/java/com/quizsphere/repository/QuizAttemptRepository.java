package com.quizsphere.repository;

import com.quizsphere.entity.QuizAttempt;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    @EntityGraph(attributePaths = {"quiz", "quiz.category"})
    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    Optional<QuizAttempt> findDetailedById(Long id);

    boolean existsByQuizId(Long quizId);
}
