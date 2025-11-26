package com.server.mcp.tool;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.QuestionType;
import com.server.interview.dto.InterviewQuestionUpdateRequestDto;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.repository.InterviewRepository;
import com.server.interview.service.InterviewQuestionService;
import com.server.interview.websocket.domain.ChatMessageEntity;
import com.server.interview.websocket.repository.ChatMessageRepository;
import com.server.jd.domain.JobDescription;
import com.server.jd.exception.JobErrorCase;
import com.server.jd.repository.JobDescriptionRepository;
import com.server.mcp.dto.InterviewQuestionAiDto;
import com.server.resume.domain.Resume;
import com.server.resume.dto.ResumeEducationRequestDto;
import com.server.resume.dto.ResumeExperienceRequestDto;
import com.server.resume.dto.ResumeSkillRequestDto;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.resume.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterviewMcpTools {
    private final ResumeService resumeService;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final InterviewQuestionService interviewQuestionService;
    private final InterviewRepository interviewRepository;
    private final ResumeRepository resumeRepository;
    private final ChatMessageRepository chatMessageRepository;

    @Transactional(readOnly = true)
    @McpTool(
            name = "get_resume",
            description = "이력서 내용을 MCP를 통해 LLM에게 제공"
    )
    public Map<String, Object> getResume(
            @McpToolParam(description = "resume id") Long resumeId
    ) {
        Resume resume = resumeRepository.findById(resumeId).orElseThrow(
                () -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND)
        );
        List<ResumeEducationRequestDto> educationList = resume.getEducations().stream()
                .map(edu -> new ResumeEducationRequestDto(
                        edu.getEducationLevel(),
                        edu.getSchoolName(),
                        edu.getMajor(),
                        edu.getIsGraduated(),
                        edu.getAdmissionDate(),
                        edu.getGraduationDate(),
                        edu.getAttendanceType(),
                        edu.getGpa(),
                        edu.getGpaScale()
                )).toList();
        List<ResumeExperienceRequestDto> experienceList = resume.getExperiences().stream()
                .map(ex -> new ResumeExperienceRequestDto(
                        ex.getCompanyName(),
                        ex.getDepartment(),
                        ex.getPosition(),
                        ex.getStartDate(),
                        ex.getEndDate()
                )).toList();
        List<ResumeSkillRequestDto> skillList = resume.getSkills().stream()
                .map(skill -> new ResumeSkillRequestDto(
                        skill.getSkill().getName(),
                        skill.getProficiencyLevel()
                )).toList();
        List<String> skillNames = resume.getSkills().stream().map(skill -> skill.getSkill().getName()).toList();
        log.info("getResume {}", resumeId);
        return Map.of(
                "experience", experienceList,
                "education", educationList,
                "skill", skillList,
                "skillNames", skillNames
        );
    }

    @Transactional(readOnly = true)
    @McpTool(
            name = "get_job_description",
            description = "직무 기술서 내용을 MCP를 통해 LLM에게 제공"
    )
    public Map<String, Object> getJobDescription(
            @McpToolParam(description = "jd id") Long jdId
    ) {
        JobDescription jobDescription = jobDescriptionRepository.findById(jdId).orElseThrow(
                () -> new ApplicationException(JobErrorCase.JOB_NOT_FOUND)
        );
        log.info("getJobDescription {}", jobDescription);
        return Map.of(
                "description", jobDescription.getDescription(),
                "experience", jobDescription.getExperience(),
                "workType", jobDescription.getWorkType(),
                "education", jobDescription.getEducation(),
                "preferredSkills", jobDescription.getPreferredSkillNames(),
                "requiredSkills", jobDescription.getRequiredSkillNames()
        );
    }

    @Transactional
    @McpTool(
            name = "save_interview_questions",
            description = "면접 질문을 생성하고 이를 DB에 저장"
    )
    public Map<String, Object> saveInterviewQuestions(
            @McpToolParam(description = "면접 id") Long interviewId,
            @McpToolParam(description = "저장할 질문 리스트") List<InterviewQuestionAiDto> questionList
    ) {
        interviewRepository.findById(interviewId).orElseThrow(
                () -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND)
        );

        var items = questionList.stream()
                .map(q -> new InterviewQuestionUpdateRequestDto.QuestionUpdateItem(
                        null,
                        QuestionType.valueOf(q.questionType().toUpperCase()),
                        q.content()
                )).toList();
        var requestDto = new InterviewQuestionUpdateRequestDto(
                items,
                List.of()
        );

        interviewQuestionService.updateQuestions(interviewId, requestDto);
        log.info("saveInterviewQuestions {}", interviewId);
        return Map.of(
                "status", "success",
                "savedCount", items.size()
        );
    }
    //인터뷰 메시지 조회
    @Transactional(readOnly = true)
    @McpTool(
            name = "get_interview_messages",
            description = "주어진 인터뷰 ID에 대한 전체 채팅 메시지 로그를 시간 순서대로 조회"
    )
    public Map<String, Object> getInterviewMessages(
            @McpToolParam(description = "면접 ID") Long interviewId
    ) {
        // 1) 인터뷰 존재 여부 검증
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // 2) 해당 인터뷰의 메시지를 시간 순으로 조회
        List<ChatMessageEntity> messages =
                chatMessageRepository.findByInterviewIdOrderByCreatedAtAsc(interview.getId());

        // 3) LLM이 이해하기 좋은 형태로 변환
        List<Map<String, Object>> messageList = messages.stream()
                .map(m -> Map.<String, Object>of(
                        "id", m.getId(),
                        "senderId", m.getSenderId(),
                        "senderName", m.getSender(),
                        "sentAt", m.getCreatedAt().toString(),
                        "content", m.getContent()
                ))
                .toList();

        log.info("[MCP] getInterviewMessages interviewId={}, count={}", interviewId, messageList.size());

        return Map.of(
                "interviewId", interviewId,
                "messages", messageList
        );
    }
    // 인터뷰 요약 저장
    @Transactional
    @McpTool(
            name = "save_interview_summary",
            description = "면접 요약 텍스트를 Interview.summary 필드에 저장"
    )
    public Map<String, Object> saveInterviewSummary(
            @McpToolParam(description = "면접 ID") Long interviewId,
            @McpToolParam(description = "AI가 생성한 요약 텍스트") String summaryText
    ) {
        // 1) 인터뷰 조회
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));

        // 2) 도메인 메서드를 통해 요약 업데이트
        interview.updateSummary(summaryText);

        log.info("[MCP] saveInterviewSummary interviewId={}, summaryLength={}",
                interviewId,
                summaryText != null ? summaryText.length() : 0
        );

        // 3) LLM에게 간단한 상태 정보 반환
        return Map.of(
                "status", "success"
        );
    }



}
