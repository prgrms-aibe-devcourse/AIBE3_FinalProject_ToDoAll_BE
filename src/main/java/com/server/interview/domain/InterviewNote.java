package com.server.interview.domain;

import com.server.global.entity.BaseEntity;
import com.server.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 면접 ID (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    // 작성자 (유저)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 면접 노트 내용
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InterviewNoteStatus status; // 질문 상태

    public static InterviewNote of(Interview interview, User author, String content, InterviewNoteStatus status) {
        InterviewNote note = new InterviewNote();
        note.interview = interview;
        note.author = author;
        note.content = content;
        note.status = status;
        return note;
    }
}
