package com.server.interview.service;

import com.server.global.exception.ApplicationException;
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
    private final InterviewParticipantRepository participantRepository;
    private final InterviewNoteMemoRepository interviewNoteMemoRepository;

    // 공통 로직: 인터뷰 + 노트 + 사용자 + 권한 체크
    private void getInterview(Long interviewId) {
        if (!interviewRepository.existsById(interviewId)) {
            throw new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND);
        }
    }

    private InterviewNote getInterviewNote(Long interviewId) {
        return interviewNoteRepository.findByInterviewId(interviewId)
                .orElseThrow(() ->
                        new ApplicationException(InterviewNoteErrorCase.INTERVIEW_NOTE_NOT_FOUND)
                );
    }

    private User getUser() {
        // TODO: JWT 인증 후 실제 userId 사용하도록 수정
        return userRepository.findById(1L)
                .orElseThrow();
    }

    private void checkPermission(Long interviewId, Long userId) {
        boolean allowed = participantRepository.existsByInterviewIdAndUserId(interviewId, userId);
        if (!allowed) {
            throw new ApplicationException(InterviewNoteMemoErrorCase.FORBIDDEN);
        }
    }

    // 메모 조회
    public List<InterviewNoteMemoSearchResponseDto> getMemos(Long interviewId) {

        // 공통 검증
        getInterview(interviewId);
        InterviewNote note = getInterviewNote(interviewId);
        User user = getUser();
        checkPermission(interviewId, user.getId());

        // 메모 DTO 변환
        return note.getMemos().stream()
                .map(InterviewNoteMemoSearchResponseDto::from)
                .toList();
    }

    // 메모 생성
    @Transactional
    public InterviewNoteMemoCreateResponseDto create(
            Long interviewId,
            InterviewNoteMemoCreateRequestDto requestDto
    ) {

        // 공통 검증
        getInterview(interviewId);
        InterviewNote note = getInterviewNote(interviewId);
        User user = getUser();
        checkPermission(interviewId, user.getId());

        // 생성
        InterviewNoteMemo memo = InterviewNoteMemo.of(
                note,
                user,
                requestDto.content()
        );
        interviewNoteMemoRepository.save(memo);

        return new InterviewNoteMemoCreateResponseDto(memo.getId());
    }
}
