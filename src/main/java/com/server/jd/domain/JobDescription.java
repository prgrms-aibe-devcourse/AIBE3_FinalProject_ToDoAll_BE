package com.server.jd.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobDescription extends BaseEntity {

    // TODO: 아래 필드들은 모두 예시입니다. 본인이 필요한것은 추가하고 필요없는것은 삭제해주세요.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 공고 제목
    private String title;

    // 부서명
    private String department;

    // 근무 지역
    private String region;

    // 근무 형태
    private String workType;

    // 경력 요건 (예시: 신입, 5년 이상 등)
    private String experience;

    // 학력 요건 (예시: 대졸 이상)
    private String education;

    // 연봉 또는 급여 정보 (예시: 연봉 4천~6천)
    private String salary;

    // 업무 설명
    private String description;

    // 마감일
    private LocalDate deadline;

    // 공고 상태 (DRAFT, OPEN, CLOSED)
    private String status;

    // 필수 스킬 목록 (예시: React, Java 등)
    @ElementCollection
    private List<String> requiredSkills;

    // 우대 스킬 목록
    @ElementCollection
    private List<String> preferredSkills;

    // 복리후생 정보 (예시: 점심 제공, 출퇴근 교통비 등)
    private String welfare;

    // 해당 공고에 지원한 지원자 수
    private Long applicantCount;
}
