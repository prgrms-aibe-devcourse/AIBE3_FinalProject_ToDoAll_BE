package com.server.interview.domain;

import com.server.global.entity.BaseEntity;
import com.server.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewNoteMemo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 노트 ID (FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private InterviewNote note;

    // 작성자 (유저)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 면접 노트 내용
    @Column(columnDefinition = "TEXT")
    private String content;

    public static InterviewNoteMemo  of(InterviewNote note, User author, String content) {
        InterviewNoteMemo noteMemo = new InterviewNoteMemo();
        noteMemo.note = note;
        noteMemo.author = author;
        noteMemo.content = content;
        return noteMemo;
    }
}
