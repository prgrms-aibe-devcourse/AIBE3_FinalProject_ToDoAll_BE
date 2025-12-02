package com.server.jd.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.server.interview.domain.QInterview;
import com.server.interview.domain.QInterviewParticipant;
import com.server.jd.domain.QJobDescription;
import com.server.jd.dto.JobDescriptionInterviewOptionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JobDescriptionRepositoryImpl implements JobDescriptionRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QInterview i = QInterview.interview;
    private final QJobDescription jd = QJobDescription.jobDescription;
    private final QInterviewParticipant ip = QInterviewParticipant.interviewParticipant;

    @Override
    public List<JobDescriptionInterviewOptionDto> findJdListByInterviewParticipant(Long userId) {
        return queryFactory
                .selectDistinct(
                        Projections.constructor(
                                JobDescriptionInterviewOptionDto.class,
                                jd.id,
                                jd.title
                        )
                )
                .from(i)
                .leftJoin(i.jobDescription, jd)
                .leftJoin(i.interviewParticipant, ip)
                .where(
                        ip.user.id.eq(userId)   // 면접 참가자인 경우
                                .or(i.organizer.id.eq(userId))  // 면접 주최자인 경우
                )
                .fetch();
    }
}
