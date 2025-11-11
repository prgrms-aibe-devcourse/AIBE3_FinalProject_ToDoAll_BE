package com.server.interview.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    // 질문 유형 (CORE, TECH, BEHAVIOR)
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    // 질문 내용
    @Column(columnDefinition = "TEXT")
    private String questionText;

    // 답변 내용
    @Column(columnDefinition = "TEXT")
    private String answer;

    public static InterviewQuestion of(Interview interview,
                                       QuestionType type,
                                       String questionText,
                                       String answer) {
        InterviewQuestion question = new InterviewQuestion();
        question.interview = interview;
        question.type = type;
        question.questionText = questionText;
        question.answer = answer;
        return question;
    }
}
