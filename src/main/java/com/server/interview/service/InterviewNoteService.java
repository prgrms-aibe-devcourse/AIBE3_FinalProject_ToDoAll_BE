package com.server.interview.service;

import com.server.global.auth.AuthUtils;
import com.server.global.exception.ApplicationException;
import com.server.interview.domain.InterviewNote;
import com.server.interview.dto.InterviewNoteSearchResponseDto;
import com.server.interview.exception.InterviewErrorCase;
import com.server.interview.exception.InterviewNoteErrorCase;
import com.server.interview.repository.InterviewNoteRepository;
import com.server.interview.repository.InterviewParticipantRepository;
import com.server.interview.repository.InterviewRepository;
import com.server.user.domain.User;
import com.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewNoteService {

    private final InterviewRepository interviewRepository;
    private final InterviewNoteRepository interviewNoteRepository;
    private final InterviewParticipantRepository participantRepository;
    private final UserRepository userRepository;

    public InterviewNoteSearchResponseDto getNote(Long interviewId) {

        // 인터뷰 존재 여부 확인
        if (!interviewRepository.existsById(interviewId)) {
            throw new ApplicationException(InterviewErrorCase.INTERVIEW_NOT_FOUND);
        }
        // 인터뷰 노트 조회
        InterviewNote note = interviewNoteRepository.findByInterviewId(interviewId)
                .orElseThrow(() ->
                        new ApplicationException(InterviewNoteErrorCase.INTERVIEW_NOTE_NOT_FOUND));

        // 사용자 조회
        Long userId = AuthUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow();

        // 권한 체크 (인터뷰어 또는 옵저버)
        boolean allowed = participantRepository.existsByInterviewIdAndUserId(interviewId, user.getId());
        if (!allowed) {
            throw new ApplicationException(InterviewNoteErrorCase.FORBIDDEN);
        }

        // DTO 변환 및 반환
        return new InterviewNoteSearchResponseDto(
                note.getId(),
                interviewId,
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
