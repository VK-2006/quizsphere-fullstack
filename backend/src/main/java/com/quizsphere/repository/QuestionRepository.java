package com.quizsphere.repository;

import com.quizsphere.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizIdOrderByIdAsc(Long quizId);
}
