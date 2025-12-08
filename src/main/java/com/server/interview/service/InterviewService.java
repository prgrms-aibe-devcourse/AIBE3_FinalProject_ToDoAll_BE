package com.server.interview.service;

import com.server.global.auth.AuthUtils;
import com.server.global.exception.ApplicationException;
import com.server.interview.domain.*;
import com.server.interview.dto.*;
import com.server.interview.event.InterviewCreatedEvent;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.exception.InterviewNoteErrorCase;
import com.server.interview.repository.*;
import com.server.jd.domain.JobDescription;
import com.server.jd.domain.Skill;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.mcp.dto.InterviewCreatedAiEvent;
import com.server.mcp.dto.InterviewFinishedAiEvent;
import com.server.resume.domain.Resume;
import com.server.resume.domain.ResumeSkill;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
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
    private final InterviewEvaluationRepository interviewEvaluationRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public InterviewCreateResponseDto create(InterviewCreateRequestDto interviewCreateRequestDto) {

        // 인터뷰 생성 로직
        JobDescription jobDescription = jobDescriptionRepository.findById(interviewCreateRequestDto.jdId()).orElseThrow(
                () -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND)
        );
        Resume resume = resumeRepository.findById(interviewCreateRequestDto.resumeId()).orElseThrow(
                ()->new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND)
        );

        Long userId = AuthUtils.getCurrentUserId();

        User organizer = userRepository.findById(userId).orElse(null); // 토큰을 통해 user_id를 가져오는 로직 필요

        LocalDateTime scheduledAt =  interviewCreateRequestDto.scheduledAt();
        InterviewStatus status = InterviewStatus.WAITING;
        InterviewResult result = InterviewResult.PENDING;

        Interview interview = Interview.of(jobDescription, resume, organizer, scheduledAt, status, result);

        interviewRepository.save(interview);

        // 인터뷰 참여자 생성 로직
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

        // 인터뷰 노트 생성 로직
        InterviewNote interviewNote = InterviewNote.of(
                interview
        );
        interviewNoteRepository.save(interviewNote);

        // 이벤트 발행 (AFTER COMMIT 리스너에서 처리됨)
        eventPublisher.publishEvent(new InterviewCreatedEvent(interview.getId(), interview.getScheduledAt()));


        //MCP 면접 질문 자동 생성 및 저장 로직
        applicationEventPublisher.publishEvent(
                new InterviewCreatedAiEvent(
                        interview.getId()
                )
        );
        return new InterviewCreateResponseDto(interview.getId());
    }

    public InterviewListResponseDto getInterviews(InterviewSearchConditionDto condition) {

        Long userId = AuthUtils.getCurrentUserId();
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
                userId,
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

        Long userId = AuthUtils.getCurrentUserId();

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // 주최자(organizer)만 삭제 가능
        User organizer = userRepository.findById(userId).orElse(null);

        if (!interview.getOrganizer().getId().equals(organizer.getId())) {
            throw new ApplicationException(InterviewErrorCase.INTERVIEW_DELETE_FORBIDDEN);
        }

        // 면접 노트 삭제 (쿼리 삭제는 cascade가 발동되지 않음)
        // 노트와 메모가 cascade 설정이 되어있지만 밑에처럼 쿼리로 노트를 삭제하면 cascade가 반영되지 않는다..!
        // interviewNoteRepository.deleteByInterviewId(interviewId);

        // 인터뷰 노트 조회
        InterviewNote note = interviewNoteRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewNoteErrorCase.INTERVIEW_NOTE_NOT_FOUND));

        // 엔티티 삭제 → cascade 로 memo 자동 삭제됨
        if (note != null) {
            interviewNoteRepository.delete(note);
        }

        // 면접 질문 삭제
        interviewQuestionRepository.deleteByInterviewId(interviewId);

        //면접 평가 삭제
        interviewEvaluationRepository.deleteByInterviewId(interviewId);

        interviewRepository.delete(interview);
    }

    // 면접을 종료(DONE) 상태로 변경하고, 면접 종료 이벤트를 발행해서 AI 요약을 비동기로 실행한다.
    @Transactional
    public void finishInterview(Long interviewId) {
        // 1) 인터뷰 조회
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // 2) 상태를 DONE 으로 변경
        interview.updateStatus(InterviewStatus.DONE);

        // 3) 면접 종료 → AI 요약 이벤트 발행
        applicationEventPublisher.publishEvent(
                new InterviewFinishedAiEvent(interview.getId())
        );
    }

    private void validateStatus(String status) {
        if (status == null || status.equals("ALL")) return;

        try {
            InterviewStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(InterviewErrorCase.INVALID_STATUS);
        }
    }

    public InterviewProfileResponseDto getInterviewProfile(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        Resume resume = interview.getResume();
        JobDescription jd = interview.getJobDescription();

        // 1) 보유 스킬 (ResumeSkill -> Skill.name)
        List<String> ownedSkills = resume.getSkills().stream()
                .map(ResumeSkill::getSkill)
                .map(Skill::getName)
                .toList();
        List<String> requiredSkills = jd.getRequiredSkillNames().stream()
                .toList();

        // 3) 부족 스킬 = JD 필요 스킬 - 보유 스킬
        Set<String> ownedLower = ownedSkills.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        List<String> missingSkills = requiredSkills.stream()
                .filter(req -> !ownedLower.contains(req.toLowerCase(Locale.ROOT)))
                .toList();

        // 4) 경력 문자열 만들기 (ResumeExperience 사용)
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<String> experiences = resume.getExperiences().stream()
                .sorted((e1, e2) -> {
                    // 시작일 기준 오름차순 정렬 (null 처리 포함)
                    if (e1.getStartDate() == null && e2.getStartDate() == null) return 0;
                    if (e1.getStartDate() == null) return 1;
                    if (e2.getStartDate() == null) return -1;
                    return e1.getStartDate().compareTo(e2.getStartDate());
                })
                .map(exp -> {
                    String period;

                    if (exp.getStartDate() == null && exp.getEndDate() == null) {
                        period = "";
                    } else if (exp.getEndDate() == null) {
                        period = String.format("(%s ~ 현재)",
                                exp.getStartDate() != null ? exp.getStartDate().format(fmt) : "");
                    } else {
                        period = String.format("(%s ~ %s)",
                                exp.getStartDate() != null ? exp.getStartDate().format(fmt) : "",
                                exp.getEndDate().format(fmt));
                    }

                    // 프론트에서 보기 좋게 한 줄로
                    return String.format(
                            "%s %s %s %s",
                            nullToEmpty(exp.getCompanyName()),   // EX
                            nullToEmpty(exp.getDepartment()),    // 개발부
                            nullToEmpty(exp.getPosition()),      // 사원
                            period
                    ).trim();
                })
                .toList();

        return new InterviewProfileResponseDto(ownedSkills, missingSkills, experiences);
    }
    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @Transactional
    public void updateResult(
            Long interviewId,
            InterviewResultUpdateRequestDto request
    ) {
        if (!interviewRepository.existsById(interviewId)) {
            throw new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND);
        }
        // result 필수 값 검증
        if (request.result() == null) {
            throw new ApplicationException(InterviewErrorCase.RESULT_REQUIRED);
        }

        // 문자열 → Enum 변환 + 유효성 체크
        InterviewResult newResult;
        try {
            newResult = InterviewResult.valueOf(request.result().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(InterviewErrorCase.INVALID_RESULT);
        }

        // PENDING 은 이 API에서 허용하지 않음
        if (newResult == InterviewResult.PENDING) {
            throw new ApplicationException(InterviewErrorCase.INVALID_RESULT);
        }

        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // 결과 업데이트
        interview.updateResult(newResult);
    }

    public InterviewSummaryDto getInterview(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // JD
        JobDescription jd = interview.getJobDescription();
        Long jdId = (jd != null) ? jd.getId() : null;
        String jdTitle = (jd != null) ? jd.getTitle() : null; // getTitle()이 없으면 JD 필드명으로 변경

        // Resume
        Resume resume = interview.getResume();
        Long resumeId = (resume != null) ? resume.getId() : null;

        // 후보자 이름/아바타는 프로젝트마다 다름 → 일단 null로 두고 프론트에서 기본 이미지 처리
        String candidateName = null;
        String candidateAvatar = null;

        // status/result
        String status = (interview.getStatus() != null) ? interview.getStatus().name() : null;
        String resultStatus = (interview.getResult() != null) ? interview.getResult().name() : null;

        // 면접관 이름 리스트도 일단 빈 리스트(필요하면 아래 2번에서 채우는 법 제공)
        List<String> interviewers = List.of();

        return new InterviewSummaryDto(
                interview.getId(),
                jdId,
                jdTitle,
                resumeId,
                candidateName,
                status,
                resultStatus,
                candidateAvatar,
                interviewers,
                interview.getScheduledAt(),
                interview.getCreatedAt()
        );
    }

}
