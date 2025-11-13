package com.server.interview.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInterviewParticipant is a Querydsl query type for InterviewParticipant
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInterviewParticipant extends EntityPathBase<InterviewParticipant> {

    private static final long serialVersionUID = -339256335L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInterviewParticipant interviewParticipant = new QInterviewParticipant("interviewParticipant");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInterview interview;

    public final DateTimePath<java.time.LocalDateTime> joinedAt = createDateTime("joinedAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> leftAt = createDateTime("leftAt", java.time.LocalDateTime.class);

    public final EnumPath<InterviewRole> role = createEnum("role", InterviewRole.class);

    public final com.server.user.domain.QUser user;

    public QInterviewParticipant(String variable) {
        this(InterviewParticipant.class, forVariable(variable), INITS);
    }

    public QInterviewParticipant(Path<? extends InterviewParticipant> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInterviewParticipant(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInterviewParticipant(PathMetadata metadata, PathInits inits) {
        this(InterviewParticipant.class, metadata, inits);
    }

    public QInterviewParticipant(Class<? extends InterviewParticipant> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.interview = inits.isInitialized("interview") ? new QInterview(forProperty("interview"), inits.get("interview")) : null;
        this.user = inits.isInitialized("user") ? new com.server.user.domain.QUser(forProperty("user")) : null;
    }

}

