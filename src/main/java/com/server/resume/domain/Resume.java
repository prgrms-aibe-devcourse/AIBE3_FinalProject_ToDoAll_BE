package com.server.resume.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Resume extends BaseEntity {

    // 필드는 모두 예시입니다. (본인이 필요한것들은 수정 및 추가하기)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이름
    private String name;

    // 이메일
    private String email;

    // 연락처
    private String phoneNumber;

    // 주소
    private String address;

    // 학력 사항 (예시: 잡다대학교 컴퓨터공학과 졸업)
    private String education;

    // 경력 사항 (예시: 잡다주식회사 프론트엔드 개발 3년 등)
    private String experience;

    // 보유 기술 스택 (예시: React, Java, AWS 등)
    private String skills;

    // 자격증, 수상 내역 등
    private String certifications;

    // 이력서 PDF 파일 경로 (S3 등 외부 저장소 링크)
    private String resumeFileUrl;

    // 포트폴리오 파일 경로 (S3 등 외부 저장소 링크)
    private String portfolioFileUrl;

    // TODO: 이력서 작성자 (userId) - 유저 관련 연관 관계 매핑 필요합니다.
    private Long userId;
}