package com.quizsphere.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "attempt_answers", uniqueConstraints = @UniqueConstraint(
        name = "uk_attempt_question", columnNames = {"attempt_id", "question_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AttemptAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answer_attempt"))
    private QuizAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, foreignKey = @ForeignKey(name = "fk_answer_question"))
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id", foreignKey = @ForeignKey(name = "fk_answer_option"))
    private QuestionOption selectedOption;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private Integer marksAwarded;
}
