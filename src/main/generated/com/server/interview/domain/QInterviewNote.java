package com.server.interview.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInterviewNote is a Querydsl query type for InterviewNote
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInterviewNote extends EntityPathBase<InterviewNote> {

    private static final long serialVersionUID = -43254540L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInterviewNote interviewNote = new QInterviewNote("interviewNote");

    public final com.server.global.entity.QBaseEntity _super = new com.server.global.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInterview interview;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInterviewNote(String variable) {
        this(InterviewNote.class, forVariable(variable), INITS);
    }

    public QInterviewNote(Path<? extends InterviewNote> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInterviewNote(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInterviewNote(PathMetadata metadata, PathInits inits) {
        this(InterviewNote.class, metadata, inits);
    }

    public QInterviewNote(Class<? extends InterviewNote> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.interview = inits.isInitialized("interview") ? new QInterview(forProperty("interview"), inits.get("interview")) : null;
    }

}

