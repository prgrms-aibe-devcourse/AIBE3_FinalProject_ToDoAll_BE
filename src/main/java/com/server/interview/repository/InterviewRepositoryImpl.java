package com.server.interview.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.interview.domain.QInterview;
import com.server.interview.domain.QInterviewParticipant;
import com.server.interview.dto.InterviewSummaryDto;
import com.server.jd.domain.QJobDescription;
import com.server.resume.domain.QResume;
import com.server.user.domain.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class InterviewRepositoryImpl implements InterviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QInterview i = QInterview.interview;
    private final QJobDescription jd = QJobDescription.jobDescription;
    private final QResume r = QResume.resume;

    @Override
    public List<InterviewSummaryDto> searchInterviews(
            Long userId,
            Long jdId,
            String status,
            Long cursor,
            String sort,
            int limit
    ) {

        List<InterviewSummaryDto> interviews = queryFactory
                .select(Projections.constructor(
                        InterviewSummaryDto.class,
                        i.id,                      // interviewId
                        jd.id,                     // jdId
                        jd.title,                  // jdTitle
                        r.id,                      // resumeId
                        r.name,                    // candidateName
                        i.status.stringValue(),    // status
                        i.result.stringValue(),     //resultStatus
                        r.resumeFileUrl,             // candidateAvatar
                        Expressions.constant(Collections.<String>emptyList()),
                        i.scheduledAt,
                        i.createdAt
                ))
                .from(i)
                .leftJoin(i.jobDescription, jd)
                .leftJoin(i.resume, r)
                .where(
                        belongsToUser(userId),
                        jdId == null ? null : jd.id.eq(jdId),
                        status == null ? null : i.status.stringValue().eq(status),
                        cursor == null ? null : i.id.lt(cursor)
                )
                .orderBy(i.createdAt.desc())
                .limit(limit)
                .fetch();

        // 면접관 정보 조인해서 interviewers 채우기
        QInterviewParticipant ip = QInterviewParticipant.interviewParticipant;
        QUser u = QUser.user;

        for (int idx = 0; idx < interviews.size(); idx++) {
            InterviewSummaryDto base = interviews.get(idx);

            List<String> interviewerNames = queryFactory
                    .select(u.name)
                    .from(ip)
                    .join(ip.user, u)
                    .where(ip.interview.id.eq(base.interviewId()))
                    .fetch();

            interviews.set(idx, new InterviewSummaryDto(
                    base.interviewId(),
                    base.jdId(),
                    base.jdTitle(),
                    base.resumeId(),
                    base.candidateName(),
                    base.status(),
                    base.resultStatus(),
                    base.candidateAvatar(),
                    interviewerNames,
                    base.scheduledAt(),
                    base.createdAt()
            ));
        }

        return interviews;
    }


    private BooleanExpression belongsToUser(Long userId) {
        if (userId == null) return null;

        return i.organizer.id.eq(userId)   // 주최자
                .or(i.interviewParticipant.any().user.id.eq(userId)); // 참여자
    }
}
