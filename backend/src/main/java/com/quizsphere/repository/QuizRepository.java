package com.quizsphere.repository;

import com.quizsphere.entity.Quiz;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    @EntityGraph(attributePaths = {"category"})
    List<Quiz> findByPublishedTrueOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"category", "questions"})
    Optional<Quiz> findWithQuestionsById(Long id);

    boolean existsByCategoryId(Long categoryId);
}
