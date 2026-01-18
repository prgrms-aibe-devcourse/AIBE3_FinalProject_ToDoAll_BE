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
import com.server.match.domain.Match;
import com.server.match.domain.MatchStatus;
import com.server.match.exception.MatchErrorCase;
import com.server.match.repository.MatchRepository;
import com.server.mcp.dto.InterviewCreatedAiEvent;
import com.server.mcp.dto.InterviewFinishedAiEvent;
import com.server.resume.domain.Resume;
import com.server.resume.domain.ResumeExperience;
import com.server.resume.domain.ResumeSkill;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.User;
import com.server.user.exception.UserErrorCase;
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
    private final ApplicationEventPublisher eventPublisher;
    private final MatchRepository matchRepository;

    @Transactional
    public InterviewCreateResponseDto create(InterviewCreateRequestDto req) {

        JobDescription jd = getJobDescription(req.jdId());
        Resume resume = getResume(req.resumeId());
        User organizer = getCurrentUser();

        Interview interview = createInterview(jd, resume, organizer, req.scheduledAt());

        registerParticipants(interview, organizer, req.participantIds());

        createInterviewNote(interview);

        confirmMatch(jd.getId(), resume.getId());

        publishCreatedEvents(interview);

        return new InterviewCreateResponseDto(interview.getId());
    }

    public InterviewListResponseDto getInterviews(InterviewSearchConditionDto condition) {
        Long userId = AuthUtils.getCurrentUserId();

        SearchParams params = normalizeCondition(condition);

        List<InterviewSummaryDto> fetched = searchInterviews(userId, params, condition.jdId());

        return toCursorResponse(fetched, params.limit());
    }

    @Transactional
    public void deleteInterview(Long interviewId) {
        Long userId = AuthUtils.getCurrentUserId();

        Interview interview = getInterviewOrThrow(interviewId);
        User currentUser = getUserOrThrow(userId);

        validateDeletePermission(interview, currentUser);

        deleteInterviewNote(interviewId);        // note + (cascade로 memo)
        deleteInterviewQuestions(interviewId);
        deleteInterviewEvaluations(interviewId);

        deleteInterviewEntity(interview);
    }


    // 면접을 종료(DONE) 상태로 변경하고, 면접 종료 이벤트를 발행해서 AI 요약을 비동기로 실행한다.
    @Transactional
    public void finishInterview(Long interviewId) {
        // 1) 인터뷰 조회
        Interview interview = getInterviewOrThrow(interviewId);

        // 2) 상태를 DONE 으로 변경
        interview.updateStatus(InterviewStatus.DONE);

        // 3) 면접 종료 → AI 요약 이벤트 발행
        eventPublisher.publishEvent(
                new InterviewFinishedAiEvent(interview.getId())
        );
    }


    public InterviewProfileResponseDto getInterviewProfile(Long interviewId) {

        Interview interview = getInterviewOrThrow(interviewId);

        Resume resume = interview.getResume();
        JobDescription jd = interview.getJobDescription();

        List<String> ownedSkills = extractOwnedSkills(resume);
        List<String> requiredSkills = extractRequiredSkills(jd);
        List<String> missingSkills = computeMissingSkills(ownedSkills, requiredSkills);

        List<String> experiences = buildExperienceStrings(resume);

        return new InterviewProfileResponseDto(ownedSkills, missingSkills, experiences);
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


    private JobDescription getJobDescription(Long jdId) {
        return jobDescriptionRepository.findById(jdId)
                .orElseThrow(() -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND));
    }

    private Resume getResume(Long resumeId) {
        return resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));
    }

    private User getCurrentUser() {
        Long userId = AuthUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));
    }

    private Interview createInterview(
            JobDescription jd,
            Resume resume,
            User organizer,
            LocalDateTime scheduledAt
    ) {
        Interview interview = Interview.of(
                jd,
                resume,
                organizer,
                scheduledAt,
                InterviewStatus.WAITING,
                InterviewResult.PENDING
        );
        return interviewRepository.save(interview);
    }

    private void registerParticipants(Interview interview, User organizer, List<Long> participantIds) {

        // organizer 먼저 등록
        InterviewParticipant organizerPart = InterviewParticipant.of(
                interview,
                organizer,
                InterviewRole.INTERVIEWER,
                LocalDateTime.now()
        );
        interviewParticipantRepository.save(organizerPart);

        // participantIds가 null/empty면 바로 종료 (원래 코드에 없던 안전장치)
        if (participantIds == null || participantIds.isEmpty()) {
            return;
        }

        // organizer 제외 + 중복 제거
        Set<Long> uniqueIds = participantIds.stream()
                .filter(id -> !Objects.equals(id, organizer.getId()))
                .collect(Collectors.toSet());

        if (uniqueIds.isEmpty()) {
            return;
        }

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

    private void createInterviewNote(Interview interview) {
        interviewNoteRepository.save(InterviewNote.of(interview));
    }

    private void confirmMatch(Long jdId, Long resumeId) {
        Match match = matchRepository.findByJobDescription_IdAndResume_Id(jdId, resumeId)
                .orElseThrow(() -> new ApplicationException(MatchErrorCase.MATCH_NOT_FOUND));

        // 이미 확정된 지원자면 중복 확정 불가
        if (match.getStatus() == MatchStatus.CONFIRMED) {
            throw new ApplicationException(MatchErrorCase.MATCH_ALREADY_CONFIRMED);
        }

        // 거절,보류된 경우 확정 불가
        if (match.getStatus() == MatchStatus.REJECTED || match.getStatus() == MatchStatus.HOLD) {
            throw new ApplicationException(MatchErrorCase.MATCH_CANNOT_BE_CONFIRMED);
        }

        match.updateStatus(MatchStatus.CONFIRMED);
    }

    private void publishCreatedEvents(Interview interview) {
        // 이벤트 발행 (AFTER COMMIT 리스너에서 처리됨)
        eventPublisher.publishEvent(new InterviewCreatedEvent(interview.getId(), interview.getScheduledAt()));

        // MCP 면접 질문 자동 생성 및 저장 로직
        eventPublisher.publishEvent(new InterviewCreatedAiEvent(interview.getId()));
    }

    private SearchParams normalizeCondition(InterviewSearchConditionDto condition) {
        String status = condition.status();
        validateStatus(status);

        String normalizedStatus = "ALL".equals(status) ? null : status;

        int limit = condition.limit() == null ? 6 : condition.limit();
        Long cursor = condition.cursor();
        String sort = condition.sort() == null ? "createdAt,desc" : condition.sort();

        return new SearchParams(normalizedStatus, cursor, sort, limit);
    }

    private void validateStatus(String status) {
        if (status == null || status.equals("ALL")) return;

        try {
            InterviewStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(InterviewErrorCase.INVALID_STATUS);
        }
    }

    private List<InterviewSummaryDto> searchInterviews(Long userId, SearchParams params, Long jdId) {
        return interviewRepository.searchInterviews(
                userId,
                jdId,
                params.status(),
                params.cursor(),
                params.sort(),
                params.limit() + 1
        );
    }

    private InterviewListResponseDto toCursorResponse(List<InterviewSummaryDto> fetched, int limit) {
        boolean hasNext = fetched.size() > limit;

        List<InterviewSummaryDto> page = fetched.stream()
                .limit(limit)
                .toList();

        Long nextCursor = hasNext
                ? page.get(page.size() - 1).interviewId()
                : null;

        return new InterviewListResponseDto(page, nextCursor, hasNext);
    }

    private record SearchParams(String status, Long cursor, String sort, int limit) {}


    private Interview getInterviewOrThrow(Long interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApplicationException(UserErrorCase.USER_NOT_FOUND));
    }

    private void validateDeletePermission(Interview interview, User currentUser) {
        if (!interview.getOrganizer().getId().equals(currentUser.getId())) {
            throw new ApplicationException(InterviewErrorCase.INTERVIEW_DELETE_FORBIDDEN);
        }
    }

    private void deleteInterviewNote(Long interviewId) {
        InterviewNote note = interviewNoteRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewNoteErrorCase.INTERVIEW_NOTE_NOT_FOUND));

        // orElseThrow라 null 체크 불필요
        interviewNoteRepository.delete(note);
    }

    private void deleteInterviewQuestions(Long interviewId) {
        interviewQuestionRepository.deleteByInterviewId(interviewId);
    }

    private void deleteInterviewEvaluations(Long interviewId) {
        interviewEvaluationRepository.deleteByInterviewId(interviewId);
    }

    private void deleteInterviewEntity(Interview interview) {
        interviewRepository.delete(interview);
    }


    private List<String> extractOwnedSkills(Resume resume) {
        return resume.getSkills().stream()
                .map(ResumeSkill::getSkill)
                .map(Skill::getName)
                .toList();
    }

    private List<String> extractRequiredSkills(JobDescription jd) {
        return jd.getRequiredSkillNames().stream().toList();
    }

    private List<String> computeMissingSkills(List<String> ownedSkills, List<String> requiredSkills) {
        Set<String> ownedLower = ownedSkills.stream()
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        return requiredSkills.stream()
                .filter(req -> !ownedLower.contains(req.toLowerCase(Locale.ROOT)))
                .toList();
    }


    private List<String> buildExperienceStrings(Resume resume) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return resume.getExperiences().stream()
                .sorted(this::compareByStartDateNullSafe)
                .map(exp -> formatExperienceLine(exp, fmt))
                .toList();
    }

    private int compareByStartDateNullSafe(ResumeExperience e1, ResumeExperience e2) {
        if (e1.getStartDate() == null && e2.getStartDate() == null) return 0;
        if (e1.getStartDate() == null) return 1;
        if (e2.getStartDate() == null) return -1;
        return e1.getStartDate().compareTo(e2.getStartDate());
    }

    private String formatExperienceLine(ResumeExperience exp, DateTimeFormatter fmt) {
        String period = formatPeriod(exp, fmt);

        return String.format(
                "%s %s %s %s",
                nullToEmpty(exp.getCompanyName()),
                nullToEmpty(exp.getDepartment()),
                nullToEmpty(exp.getPosition()),
                period
        ).trim();
    }

    private String formatPeriod(ResumeExperience exp, DateTimeFormatter fmt) {
        if (exp.getStartDate() == null && exp.getEndDate() == null) {
            return "";
        }
        if (exp.getEndDate() == null) {
            return String.format("(%s ~ 현재)",
                    exp.getStartDate() != null ? exp.getStartDate().format(fmt) : "");
        }
        return String.format("(%s ~ %s)",
                exp.getStartDate() != null ? exp.getStartDate().format(fmt) : "",
                exp.getEndDate().format(fmt));
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }


}
