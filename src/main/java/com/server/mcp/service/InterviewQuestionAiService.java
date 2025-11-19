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
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class InterviewQuestionAiService {

    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository participantRepository;
    private final InterviewQuestionRepository questionRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;

    // ===== 공통 유틸 (기존 InterviewQuestionService 로직 최대한 재사용) =====

    private Interview getInterview(Long interviewId) {
        return interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND));
    }

    private User getUser() {
        // TODO: JWT 인증 후 실제 userId 사용하도록 교체
        return userRepository.findById(2L)
                .orElseThrow();
    }

    private void checkPermission(Long interviewId, Long userId) {
        boolean allowed = participantRepository.existsByInterviewIdAndUserId(interviewId, userId);
        if (!allowed) {
            throw new ApplicationException(InterviewQuestionErrorCase.FORBIDDEN);
        }
    }

    private Resume getResumeFromInterview(Interview interview) {
        Resume resume = interview.getResume();   // Interview.of(...) 에서 Resume 넣고 있으므로 보통 이렇게 접근 가능
        if (resume == null) {
            throw new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND);
        }
        // 혹시 LAZY 로딩 문제나 세부 정보 부족하면, id 로 다시 조회
        return resumeRepository.findByIdWithDetails(resume.getId())
                .orElseThrow(() -> new ApplicationException(ResumeErrorCase.RESUME_NOT_FOUND));
    }


    // * LLM에게 넘겨줄 이력서 텍스트 생성.

    private String buildResumeText(Resume resume) {
        try {
            ResumeResponseDto dto = ResumeResponseDto.fromEntity(resume);
            // LLM이 구조를 이해하기 좋도록 JSON으로 넘김
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            // 실패하면 최소한 toString 이라도
            return resume.toString();
        }
    }

    private QuestionType mapCategoryToQuestionType(String raw) {
        if (raw == null) return QuestionType.CORE;
        String c = raw.trim().toUpperCase();

        return switch (c) {
            case "BEHAVIOR", "BEHAVIOUR", "행동" -> QuestionType.BEHAVIOR;
            case "TECH", "TECHNICAL", "기술" -> QuestionType.TECH;
            default -> QuestionType.CORE;
        };
    }

    // ====== 실제 AI 기반 질문 자동 생성 메서드 ======

    @Transactional
    public List<InterviewQuestionResponseDto> generateQuestionsByAi(Long interviewId, int count) {

        // 1. 인터뷰 & 권한 체크
        Interview interview = getInterview(interviewId);
        User user = getUser();
        checkPermission(interviewId, user.getId());

        // 2. 이력서 로드 + 텍스트 변환
        Resume resume = getResumeFromInterview(interview);
        String resumeText = buildResumeText(resume);

        // 3. LLM 프롬프트
        String prompt = """
                너는 채용 면접관이다.
                아래 JSON 형식의 이력서 정보를 참고해서, 해당 지원자에게 물어볼 만한 면접 질문을 %d개 생성해라.

                출력 형식은 반드시 아래 JSON 형식만 사용해라.
                설명 문장, 마크다운, 자연어 코멘트는 절대 쓰지 말고 JSON만 출력해라.

                {
                  "questions": [
                    {
                      "question": "질문 내용",
                      "category": "BEHAVIOR | CORE | TECH 중 하나",
                      "level": "HIGH | MEDIUM | LOW 중 하나"
                    }
                  ]
                }

                이력서 JSON:
                %s
                """.formatted(count, resumeText);

        String llmOutput = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        // 4. JSON 파싱 → InterviewQuestion 엔티티 생성
        List<InterviewQuestion> newQuestions = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(llmOutput);
            JsonNode questionsNode = root.path("questions");

            if (questionsNode.isArray()) {
                for (JsonNode qNode : questionsNode) {
                    String qText = qNode.path("question").asText();
                    String category = qNode.path("category").asText("CORE");

                    if (qText == null || qText.isBlank()) continue;

                    QuestionType questionType = mapCategoryToQuestionType(category);

                    InterviewQuestion question = InterviewQuestion.of(
                            interview,
                            questionType,
                            qText,
                            QuestionStatus.PENDING, // 처음에는 답변 안 된 상태
                            false                   // checked 기본 false
                    );
                    newQuestions.add(question);
                }
            }
        } catch (Exception e) {
            // LLM이 JSON을 망가뜨렸을 때를 위한 fallback: 줄 단위로 질문만 뽑기
            for (String line : llmOutput.split("\n")) {
                String trimmed = line.replaceAll("^[\\-•\\d\\.\\s]+", "").trim();
                if (trimmed.isBlank()) continue;

                InterviewQuestion question = InterviewQuestion.of(
                        interview,
                        QuestionType.CORE,
                        trimmed,
                        QuestionStatus.PENDING,
                        false
                );
                newQuestions.add(question);
            }
        }

        if (newQuestions.isEmpty()) {
            throw new ApplicationException(InterviewQuestionErrorCase.INVALID_FORMAT); // 적당한 에러 케이스 재사용
        }

        // 5. DB 저장
        List<InterviewQuestion> saved = questionRepository.saveAll(newQuestions);

        // 6. 기존 ResponseDto 재사용해서 반환
        return saved.stream()
                .map(q -> new InterviewQuestionResponseDto(
                        q.getId(),
                        q.getType().name(),
                        q.getQuestionText(),
                        q.getCreatedAt(),
                        q.getUpdatedAt()
                ))
                .toList();
    }
}