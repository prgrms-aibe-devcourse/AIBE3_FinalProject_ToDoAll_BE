package com.server.interview.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInterviewNoteMemo is a Querydsl query type for InterviewNoteMemo
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInterviewNoteMemo extends EntityPathBase<InterviewNoteMemo> {

    private static final long serialVersionUID = 1017179214L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInterviewNoteMemo interviewNoteMemo = new QInterviewNoteMemo("interviewNoteMemo");

    public final com.server.global.entity.QBaseEntity _super = new com.server.global.entity.QBaseEntity(this);

    public final com.server.user.domain.QUser author;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QInterviewNote note;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInterviewNoteMemo(String variable) {
        this(InterviewNoteMemo.class, forVariable(variable), INITS);
    }

    public QInterviewNoteMemo(Path<? extends InterviewNoteMemo> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInterviewNoteMemo(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInterviewNoteMemo(PathMetadata metadata, PathInits inits) {
        this(InterviewNoteMemo.class, metadata, inits);
    }

    public QInterviewNoteMemo(Class<? extends InterviewNoteMemo> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new com.server.user.domain.QUser(forProperty("author")) : null;
        this.note = inits.isInitialized("note") ? new QInterviewNote(forProperty("note"), inits.get("note")) : null;
    }

}

