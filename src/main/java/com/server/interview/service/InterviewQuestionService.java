package com.server.interview.service;

import com.server.interview.repository.InterviewQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewQuestionService {
    private final InterviewQuestionRepository intervierQuestionRepository;
}
