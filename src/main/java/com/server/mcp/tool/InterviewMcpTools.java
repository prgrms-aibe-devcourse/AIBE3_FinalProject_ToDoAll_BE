package com.server.mcp.tool;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.QuestionType;
import com.server.interview.dto.InterviewQuestionUpdateRequestDto;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.repository.InterviewRepository;
import com.server.interview.service.InterviewQuestionService;
import com.server.interview.service.InterviewService;
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
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InterviewMcpTools {
    private final ResumeService resumeService;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final InterviewQuestionService interviewQuestionService;
    private final InterviewRepository interviewRepository;
    private final ResumeRepository resumeRepository;

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

        return Map.of(
                "experience", experienceList,
                "education", educationList,
                "skill", skillList,
                "skillNames", skillNames
        );
    }

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
        return Map.of(
                "description", jobDescription.getDescription(),
                "experience", jobDescription.getExperience(),
                "workType", jobDescription.getWorkType(),
                "education", jobDescription.getEducation(),
                "preferredSkills", jobDescription.getPreferredSkillNames(),
                "requiredSkills", jobDescription.getRequiredSkillNames()
        );
    }

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

        return Map.of(
                "status", "success",
                "savedCount", items.size()
        );
    }

}
