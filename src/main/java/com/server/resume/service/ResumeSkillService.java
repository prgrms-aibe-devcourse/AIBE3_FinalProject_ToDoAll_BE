package com.server.resume.service;


import com.server.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ResumeSkillService {

    private final ResumeRepository resumeRepository;
}
