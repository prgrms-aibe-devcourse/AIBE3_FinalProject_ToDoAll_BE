package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewParticipant;
import com.server.interview.domain.InterviewRole;
import com.server.interview.domain.InterviewStatus;
import com.server.interview.dto.InterviewCreateRequestDto;
import com.server.interview.dto.InterviewCreateResponseDto;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.repository.InterviewRepository;
import com.server.jd.domain.JobDescription;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.resume.domain.Resume;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewService {
    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    @Transactional
    public InterviewCreateResponseDto create(InterviewCreateRequestDto interviewCreateRequestDto) {

        //********************* 인터뷰 생성 로직 *************************//
        JobDescription jobDescription = jobDescriptionRepository.findById(interviewCreateRequestDto.jd_id()).orElseThrow(
                () -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND)
        );
        Resume resume = resumeRepository.findById(interviewCreateRequestDto.resume_id()).orElseThrow(
                ()->new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND)
        );

        User organizer = userRepository.findById(1L).orElse(null); // 토큰을 통해 user_id를 가져오는 로직 필요

        LocalDateTime scheduledAt =  interviewCreateRequestDto.scheduledAt();
        InterviewStatus status = InterviewStatus.WAITING;

        Interview interview = Interview.of(jobDescription, resume, organizer, scheduledAt, status);

        interviewRepository.save(interview);
        //********************* 인터뷰 생성 로직 *************************//

        //********************* 인터뷰 참여자 생성 로직 *************************//
        // organizer 먼저 등록
        InterviewParticipant organizerPart =
                InterviewParticipant.of(interview, organizer, InterviewRole.INTERVIEWER, LocalDateTime.now());
        interviewParticipantRepository.save(organizerPart);

        // observer 준비
        List<Long> ids = interviewCreateRequestDto.participant_ids();

        // filter로 organizer 제외 + HashSet으로 중복 참여자 제외
        Set<Long> uniqueIds = ids.stream()
                .filter(id -> !id.equals(organizer.getId()))
                .collect(Collectors.toSet()); //Collectors.toSet() → 실제 구현은 HashSet

        // observer가 존재 하지 않으면 생성 X
        if (!uniqueIds.isEmpty()) {
            List<User> participants = userRepository.findAllById(uniqueIds);

            List<InterviewParticipant> observers = participants.stream()
                    .map(user -> InterviewParticipant.of(
                            interview,
                            user,
                            InterviewRole.OBSERVER,
                            LocalDateTime.now()
                    ))
                    .toList();

            interviewParticipantRepository.saveAll(observers);
        }
        //********************* 인터뷰 참여자 생성 로직 *************************//

        return new  InterviewCreateResponseDto(interview.getId());
    }
}
