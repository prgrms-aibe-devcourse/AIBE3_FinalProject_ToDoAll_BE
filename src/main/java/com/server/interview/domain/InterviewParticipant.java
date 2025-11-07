package com.server.interview.domain;

import jakarta.persistence.*;
import lombok.*;
import com.server.user.domain.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class InterviewParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 면접 연관관계 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    // 유저 연관관계 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 참여자 역할 (INTERVIEWER, OBSERVER)
    @Enumerated(EnumType.STRING)
    private InterviewRole role;

    // 참여 시간
    private LocalDateTime joinedAt;

    // 퇴장 시간
    private LocalDateTime leftAt;
}
