package com.server.interview.service;

import com.server.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewService {
    private final InterviewRepository interviewRepository;
}
