package com.server.interview.scheduler;

import com.server.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewScheduler {

    private final InterviewRepository interviewRepository;

    @Scheduled(fixedRate = 60000, initialDelay = 60000)
    @Transactional
    public void autoStartInterview(){
        LocalDateTime now = LocalDateTime.now();
        interviewRepository.updateInProgress(now);
    }
}
