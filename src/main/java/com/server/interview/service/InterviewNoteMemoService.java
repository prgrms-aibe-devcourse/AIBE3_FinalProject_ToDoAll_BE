package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.domain.Interview;
import com.server.interview.domain.InterviewNote;
import com.server.interview.domain.InterviewNoteMemo;
import com.server.interview.dto.InterviewNoteMemoCreateRequestDto;
import com.server.interview.dto.InterviewNoteMemoCreateResponseDto;
import com.server.interview.dto.InterviewNoteMemoSearchResponseDto;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.exception.InterviewNoteErrorCase;
import com.server.interview.exception.InterviewNoteMemoErrorCase;
import com.server.interview.repository.InterviewNoteMemoRepository;
import com.server.interview.repository.InterviewNoteRepository;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.repository.InterviewRepository;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final InterviewParticipantRepository ParticipantRepository;
    private final InterviewNoteMemoRepository interviewNoteMemoRepository;

    public List<InterviewNoteMemoSearchResponseDto> getMemos(Long interviewId) {

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
                .map(InterviewNoteMemoSearchResponseDto::from)
                .toList();
    }

    @Transactional
    public InterviewNoteMemoCreateResponseDto create(Long interviewId, InterviewNoteMemoCreateRequestDto requestDto) {
        // 인터뷰 존재 여부 확인
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() ->
                        new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND)
                );

        // 인터뷰 노트 조회
        InterviewNote note = interviewNoteRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new ApplicationException(InterviewNoteErrorCase.INTERVIEW_NOTE_NOT_FOUND));

        // 사용자 (토큰 대신 임시 user=1)
        User user = userRepository.findById(1L)
                .orElseThrow();

        // 작성 권한 체크
        boolean allowed = ParticipantRepository.existsByInterviewIdAndUserId(interviewId, user.getId());
        if (!allowed) {
            throw new ApplicationException(InterviewNoteMemoErrorCase.FORBIDDEN);
        }

        // 메모 생성
        InterviewNoteMemo memo = InterviewNoteMemo.of(
                note,
                user,
                requestDto.content()
        );

        interviewNoteMemoRepository.save(memo);

        return new InterviewNoteMemoCreateResponseDto(memo.getId());}
}
