package com.server.interview.websocket.service;

import com.server.interview.websocket.domain.ChatMessageEntity;
import com.server.interview.websocket.dto.ChatMessageResponseDto;
import com.server.interview.websocket.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InterviewHistoryServiceTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @InjectMocks
    private InterviewHistoryService interviewHistoryService;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 채팅_히스토리_정상_조회() {

        Long interviewId = 1L;

        ChatMessageEntity msg1 = ChatMessageEntity.builder()
                .id(1L)
                .interviewId(interviewId)
                .senderId(10L)
                .sender("홍길동")
                .content("첫 번째 메시지")
                .createdAt(LocalDateTime.of(2024, 11, 20, 12, 0))
                .build();

        ChatMessageEntity msg2 = ChatMessageEntity.builder()
                .id(2L)
                .interviewId(interviewId)
                .senderId(11L)
                .sender("김철수")
                .content("두 번째 메시지")
                .createdAt(LocalDateTime.of(2024, 11, 20, 12, 1))
                .build();

        when(chatMessageRepository.findByInterviewIdOrderByCreatedAtAsc(interviewId))
                .thenReturn(List.of(msg1, msg2));


        List<ChatMessageResponseDto> result = interviewHistoryService.getChatHistory(interviewId);


        verify(chatMessageRepository, times(1))
                .findByInterviewIdOrderByCreatedAtAsc(interviewId);

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getSenderId()).isEqualTo(10L);
        assertThat(result.get(0).getSender()).isEqualTo("홍길동");
        assertThat(result.get(0).getContent()).isEqualTo("첫 번째 메시지");

        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getSenderId()).isEqualTo(11L);
        assertThat(result.get(1).getSender()).isEqualTo("김철수");
        assertThat(result.get(1).getContent()).isEqualTo("두 번째 메시지");
    }
}
