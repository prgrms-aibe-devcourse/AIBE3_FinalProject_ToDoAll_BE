package com.server.interview.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInterviewEvaluation is a Querydsl query type for InterviewEvaluation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInterviewEvaluation extends EntityPathBase<InterviewEvaluation> {

    private static final long serialVersionUID = 268634814L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInterviewEvaluation interviewEvaluation = new QInterviewEvaluation("interviewEvaluation");

    public final com.server.global.entity.QBaseEntity _super = new com.server.global.entity.QBaseEntity(this);

    public final StringPath comment = createString("comment");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final com.server.user.domain.QUser evaluator;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInterview interview;

    public final EnumPath<InterviewResult> result = createEnum("result", InterviewResult.class);

    public final NumberPath<Integer> scoreComm = createNumber("scoreComm", Integer.class);

    public final NumberPath<Integer> scoreOverall = createNumber("scoreOverall", Integer.class);

    public final NumberPath<Integer> scoreTech = createNumber("scoreTech", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInterviewEvaluation(String variable) {
        this(InterviewEvaluation.class, forVariable(variable), INITS);
    }

    public QInterviewEvaluation(Path<? extends InterviewEvaluation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInterviewEvaluation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInterviewEvaluation(PathMetadata metadata, PathInits inits) {
        this(InterviewEvaluation.class, metadata, inits);
    }

    public QInterviewEvaluation(Class<? extends InterviewEvaluation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.evaluator = inits.isInitialized("evaluator") ? new com.server.user.domain.QUser(forProperty("evaluator")) : null;
        this.interview = inits.isInitialized("interview") ? new QInterview(forProperty("interview"), inits.get("interview")) : null;
    }

}

