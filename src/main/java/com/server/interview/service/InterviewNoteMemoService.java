package com.server.interview.service;

import com.server.interview.repository.InterviewNoteMemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewNoteMemoService {
    private final InterviewNoteMemoRepository interviewNoteMemoRepository;

}
