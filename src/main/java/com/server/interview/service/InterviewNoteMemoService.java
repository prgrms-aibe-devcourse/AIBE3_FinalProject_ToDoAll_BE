package com.server.interview.service;

import com.server.global.auth.AuthUtils;
import com.server.global.exception.ApplicationException;
import com.server.interview.domain.InterviewNote;
import com.server.interview.domain.InterviewNoteMemo;
import com.server.interview.dto.*;
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
    private final InterviewNoteMemoRepository memoRepository;

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
        Long userId = AuthUtils.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow();
    }

    private void checkPermission(Long interviewId, Long userId) {
        boolean allowed = participantRepository.existsByInterviewIdAndUserId(interviewId, userId);
        if (!allowed) {
            throw new ApplicationException(InterviewNoteMemoErrorCase.FORBIDDEN);
        }
    }

    private InterviewNoteMemo getMemo(Long memoId) {
        return memoRepository.findById(memoId)
                .orElseThrow(() ->
                        new ApplicationException(InterviewNoteMemoErrorCase.INTERVIEW_MEMO_NOT_FOUND)
                );
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
        memoRepository.save(memo);

        return new InterviewNoteMemoCreateResponseDto(memo.getId());
    }

    // 메모 수정 API
    @Transactional
    public InterviewNoteMemoUpdateResponseDto update(
            Long interviewId,
            Long memoId,
            InterviewNoteMemoUpdateRequestDto request
    ) {
        // 인터뷰 존재 확인
        getInterview(interviewId);

        // 인터뷰 노트 확인
        InterviewNote note = getInterviewNote(interviewId);

        // 권한 확인
        User user = getUser();
        checkPermission(interviewId, user.getId());

        // 메모 조회
        InterviewNoteMemo memo = getMemo(memoId);

        // 메모가 해당 인터뷰 노트에 속하는지 확인
        if (!memo.getNote().getId().equals(note.getId())) {
            throw new ApplicationException(InterviewNoteMemoErrorCase.INTERVIEW_MEMO_NOT_FOUND);
        }
        // 7)  본인 메모인지 확인
        if (!memo.getAuthor().getId().equals(user.getId())) {
            throw new ApplicationException(InterviewNoteMemoErrorCase.FORBIDDEN);
        }

        // 내용 수정
        memo.updateContent(request.content());

        // 응답 생성
        return new InterviewNoteMemoUpdateResponseDto(
                memo.getId(),
                memo.getContent(),
                memo.getUpdatedAt()
        );
    }
}
