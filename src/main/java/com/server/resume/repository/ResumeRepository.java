package com.server.resume.repository;

import com.server.resume.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    // 이력서 제출한 지원자 전체 조회 관련
    List<Resume> findAllByUserId(Long userId);
}