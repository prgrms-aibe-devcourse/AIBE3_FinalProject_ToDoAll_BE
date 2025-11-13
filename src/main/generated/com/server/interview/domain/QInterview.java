package com.server.interview.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInterview is a Querydsl query type for Interview
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInterview extends EntityPathBase<Interview> {

    private static final long serialVersionUID = 2043320802L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInterview interview = new QInterview("interview");

    public final com.server.global.entity.QBaseEntity _super = new com.server.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInterviewEvaluation interviewEvaluation;

    public final QInterviewNote interviewNote;

    public final com.server.jd.domain.QJobDescription jobDescription;

    public final com.server.user.domain.QUser organizer;

    public final com.server.resume.domain.QResume resume;

    public final DateTimePath<java.time.LocalDateTime> scheduledAt = createDateTime("scheduledAt", java.time.LocalDateTime.class);

    public final EnumPath<InterviewStatus> status = createEnum("status", InterviewStatus.class);

    public final StringPath summary = createString("summary");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInterview(String variable) {
        this(Interview.class, forVariable(variable), INITS);
    }

    public QInterview(Path<? extends Interview> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInterview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInterview(PathMetadata metadata, PathInits inits) {
        this(Interview.class, metadata, inits);
    }

    public QInterview(Class<? extends Interview> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.interviewEvaluation = inits.isInitialized("interviewEvaluation") ? new QInterviewEvaluation(forProperty("interviewEvaluation"), inits.get("interviewEvaluation")) : null;
        this.interviewNote = inits.isInitialized("interviewNote") ? new QInterviewNote(forProperty("interviewNote"), inits.get("interviewNote")) : null;
        this.jobDescription = inits.isInitialized("jobDescription") ? new com.server.jd.domain.QJobDescription(forProperty("jobDescription"), inits.get("jobDescription")) : null;
        this.organizer = inits.isInitialized("organizer") ? new com.server.user.domain.QUser(forProperty("organizer")) : null;
        this.resume = inits.isInitialized("resume") ? new com.server.resume.domain.QResume(forProperty("resume"), inits.get("resume")) : null;
    }

}

