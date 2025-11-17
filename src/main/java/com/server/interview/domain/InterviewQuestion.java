package com.server.interview.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @Column(nullable = false)
    private QuestionType type;

    // 질문 내용
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    // 답변 내용
    @Column(columnDefinition = "TEXT")
    private String answer;

    // 질문 상태
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionStatus status;

    @Column(nullable = false, name = "checked")
    private boolean checked;

    public static InterviewQuestion of(Interview interview,
                                       QuestionType type,
                                       String questionText,
                                       QuestionStatus status,
                                       boolean checked) {
        InterviewQuestion question = new InterviewQuestion();
        question.interview = interview;
        question.type = type;
        question.questionText = questionText;
        question.status = status;
        question.checked = checked;
        return question;
    }

    public void update(
            @NotNull(message = "질문 유형은 필수입니다.")
            QuestionType questionType,

            @NotBlank(message = "질문 내용을 입력해주세요.")
            String content
    ) {
        this.type = questionType;
        this.questionText = content;
    }

    public void toggleCheck() {
        this.checked = !this.checked;
    }
}
