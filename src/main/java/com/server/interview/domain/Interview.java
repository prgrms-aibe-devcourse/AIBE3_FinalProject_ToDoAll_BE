package com.server.interview.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Interview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long jdId; // 공고 ID
    private Long userId; // 지원자 ID

    @Enumerated(EnumType.STRING)
    private InterviewStatus status; // 진행 상태 (WAITING, IN_PROGRESS, DONE)

    private String summary; // 면접 요약 결과

    private String interviewerNote; // 면접관 메모
}
