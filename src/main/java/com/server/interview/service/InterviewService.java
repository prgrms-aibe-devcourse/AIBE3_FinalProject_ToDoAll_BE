package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewParticipant;
import com.server.interview.domain.InterviewRole;
import com.server.interview.domain.InterviewStatus;
import com.server.interview.dto.*;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.repository.InterviewNoteRepository;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.repository.InterviewQuestionRepository;
import com.server.interview.repository.InterviewRepository;
import com.server.jd.domain.JobDescription;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.resume.domain.Resume;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewService {
    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository interviewParticipantRepository;
    private final InterviewNoteRepository interviewNoteRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    @Transactional
    public InterviewCreateResponseDto create(InterviewCreateRequestDto interviewCreateRequestDto) {

        //********************* 인터뷰 생성 로직 *************************//
        JobDescription jobDescription = jobDescriptionRepository.findById(interviewCreateRequestDto.jdId()).orElseThrow(
                () -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND)
        );
        Resume resume = resumeRepository.findById(interviewCreateRequestDto.resumeId()).orElseThrow(
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
        List<Long> ids = interviewCreateRequestDto.participantIds();

        // filter로 organizer 제외 + HashSet으로 중복 참여자 제외
        Set<Long> uniqueIds = ids.stream()
                .filter(id -> !Objects.equals(id, organizer.getId())) //Objects.equals(a, b) -> 절대 NPE가 발생하지 않는 equals 비교
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

        return new InterviewCreateResponseDto(interview.getId());
    }

    @Transactional
    public InterviewListResponseDto getInterviews(InterviewSearchCondition condition) {

        Long jdId = condition.jdId();
        String status = condition.status();

        // status 값 검증
        validateStatus(status);

        // "ALL" → null 처리
        status = "ALL".equals(status) ? null : status;

        int limit = condition.limit() == null ? 6 : condition.limit();
        Long cursor = condition.cursor();
        String sort = condition.sort() == null ? "createdAt,desc" : condition.sort();

        // 검색
        List<InterviewSummaryDto> summaries = interviewRepository.searchInterviews(
                jdId,
                status,
                cursor,
                sort,
                limit + 1
        );

        boolean hasNext = summaries.size() > limit;
        Long nextCursor = null;

        if (hasNext) {
            nextCursor = summaries.get(limit - 1).interviewId();
        }

        summaries = summaries.stream().limit(limit).toList();

        return new InterviewListResponseDto(summaries, nextCursor, hasNext);
    }


    @Transactional
    public void deleteInterview(Long interviewId) {

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // 주최자(organizer)만 삭제 가능
        User organizer = userRepository.findById(1L).orElse(null); // 토큰을 통해 user_id를 가져오는 로직 필요

        // 면접 노트 삭제
        interviewNoteRepository.deleteByInterviewId(interviewId);

        // 면접 질문 삭제
        interviewQuestionRepository.deleteByInterviewId(interviewId);

        if (!interview.getOrganizer().getId().equals(organizer.getId())) {
            throw new ApplicationException(InterviewErrorCase.INTERVIEW_DELETE_FORBIDDEN);
        }

        interviewRepository.delete(interview);
    }

    private void validateStatus(String status) {
        if (status == null || status.equals("ALL")) return;

        try {
            InterviewStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(InterviewErrorCase.INVALID_STATUS);
        }
    }
}
