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
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewQuestionAiService {

    private final InterviewRepository interviewRepository;
    private final InterviewParticipantRepository participantRepository;
    private final InterviewQuestionRepository questionRepository;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ChatClient chatClient;
    private final RestTemplate restTemplate;

    @Autowired
    public InterviewQuestionAiService(InterviewRepository interviewRepository,
                                      InterviewParticipantRepository participantRepository,
                                      InterviewQuestionRepository questionRepository,
                                      ResumeRepository resumeRepository,
                                      UserRepository userRepository,
                                      ObjectMapper objectMapper,
                                      ChatClient.Builder chatClientBuilder) {
        this.interviewRepository = interviewRepository;
        this.participantRepository = participantRepository;
        this.questionRepository = questionRepository;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.chatClient = chatClientBuilder.build();
        this.restTemplate = new RestTemplate();
    }

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
    public void requestAutoQuestionGenerate(Long interviewId, Long resumeId, Long jdId) {
        var request = Map.of(
                "interviewId", interviewId,
                "resumeId", resumeId,
                "jdId", jdId
        );

        restTemplate.postForEntity(
                "http://localhost:8090/api/ai/interviews/generate-questions",
                request,
                Void.class
        );
        System.out.println("End RequestAutoQuestionGenerate");
    }

    private String buildPrompt(Long interviewId, Long resumeId, Long jdId) {

        return """
    너는 채용 면접 질문을 자동으로 생성하는 Assistant이다.
    네가 DB나 서버 데이터를 조회하거나 저장할 때는 반드시 MCP tool만 사용해야 한다.
    임의로 HTTP 요청을 만들거나, JSON을 직접 반환해서는 안 된다.

    [사용 가능한 MCP tool]

    1) get_resume
       - 설명: 이력서 상세 정보를 가져온다.
       - 파라미터:
         - resumeId (Long): 이력서 ID

    2) get_job_description
       - 설명: 직무 기술서(JD) 정보를 가져온다.
       - 파라미터:
         - jobDescriptionId (Long): JD ID

    3) save_interview_questions
       - 설명: 생성한 면접 질문들을 해당 인터뷰에 저장한다.
       - 파라미터:
         - interviewId (Long): 면접 ID
         - questions (List<object>): 질문 리스트
           각 원소는 다음 필드를 가진다.
           - questionType (String): "CORE", "TECH", "BEHAVIOR" 중 하나
           - content (String): 실제 질문 내용 (한국어로 작성)

    [현재 인터뷰 컨텍스트]

    - interviewId = %d
    - resumeId = %d
    - jobDescriptionId = %d

    [해야 할 작업]

    1. get_resume(resumeId = %d) MCP tool을 호출해서 이력서 정보를 가져온다.
    2. get_job_description(jobDescriptionId = %d) MCP tool을 호출해서 JD 정보를 가져온다.
    3. 이력서와 JD를 비교하여, 다음 기준을 만족하는 면접 질문 10개를 생성한다.
       - 질문은 모두 한국어로 작성한다.
       - questionType:
         * CORE: 인성/지원동기/커리어 방향 등 전반적인 질문
         * TECH: JD에 나온 기술 스택, 프로젝트 경험, 문제 해결 능력 등 기술 질문
         * BEHAVIOR: 협업, 갈등 해결, 리더십, 실패 경험 등 행동 기반 질문
       - content:
         * 실제 면접에서 바로 사용할 수 있을 만큼 구체적이어야 한다.
         * "자기소개 해보세요"처럼 너무 포괄적인 질문만 만들지 말고,
           이력서와 JD에 나온 키워드를 활용해 구체적인 맥락을 붙인다.

    4. 생성한 질문들을 questions 배열로 구성한 후,
       save_interview_questions(
         interviewId = %d,
         questions = [
           { "questionType": "CORE",  "content": "..." },
           { "questionType": "TECH",  "content": "..." },
           { "questionType": "BEHAVIOR", "content": "..." },
           ...
         ]
       ) MCP tool을 한 번 호출하여 저장한다.

    5. 모든 질문이 성공적으로 저장되면 최종 응답으로는
       문자열 "DONE" 만 출력한다.
       그 외의 설명, JSON, 마크다운, 자연어 코멘트를 절대 출력하지 않는다.

    [중요 규칙]

    - 반드시 위에 정의된 MCP tool들(get_resume, get_job_description, save_interview_questions)만 사용해야 한다.
    - 존재하지 않는 tool 이름을 만들거나, 잘못된 파라미터 이름을 사용하지 말 것.
    - JSON을 직접 반환하지 말고, 오직 MCP tool 호출을 통해서만 DB에 접근할 것.
    """.formatted(
                interviewId,
                resumeId,
                jdId,
                resumeId,
                jdId,
                interviewId
        );
    }
}