
package com.server.mcp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewQuestion;
import com.server.interview.domain.QuestionStatus;
import com.server.interview.domain.QuestionType;
import com.server.interview.dto.InterviewQuestionResponseDto;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.exception.InterviewQuestionErrorCase;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.repository.InterviewQuestionRepository;
import com.server.interview.repository.InterviewRepository;
import com.server.resume.domain.Resume;
import com.server.resume.dto.ResumeResponseDto;
import com.server.resume.exception.ResumeErrorCase;
import com.server.resume.repository.ResumeRepository;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InterviewQuestionAiService {

    private final RestTemplate restTemplate;

    public void requestAutoQuestionGenerate(Long interviewId) {
        var request = Map.of(
                "interviewId", interviewId
        );

        restTemplate.postForEntity(
                "http://localhost:8090/api/ai/interviews/generate-questions",
                request,
                Void.class
        );
        System.out.println("End RequestAutoQuestionGenerate");
    }
}
