package com.server.interview.websocket.service;

import com.server.interview.websocket.dto.ChatMessageResponseDto;
import com.server.interview.websocket.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterviewHistoryService {

    private final ChatMessageRepository chatMessageRepository;


    public List<ChatMessageResponseDto> getChatHistory(Long interviewId) {
        return chatMessageRepository.findByInterviewIdOrderByCreatedAtAsc(interviewId)
                .stream()
                .map(ChatMessageResponseDto::from)
                .toList();
    }
}
