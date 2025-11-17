package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewNote;
import com.server.interview.dto.InterviewNoteMemoResponseDto;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.exception.InterviewNoteErrorCase;
import com.server.interview.repository.InterviewNoteRepository;
import com.server.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewNoteMemoService {
    private final InterviewRepository interviewRepository;
    private final InterviewNoteRepository interviewNoteRepository;

    public List<InterviewNoteMemoResponseDto> getMemos(Long interviewId) {

        // 인터뷰 존재 여부 확인
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() ->
                        new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND)
                );

        // 인터뷰 노트 조회
        InterviewNote note = interviewNoteRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewNoteErrorCase.INTERVIEW_NOTE_NOT_FOUND));

        // 메모 리스트 → DTO 변환
        return note.getMemos().stream()
                .map(InterviewNoteMemoResponseDto::from)
                .toList();
    }
}
