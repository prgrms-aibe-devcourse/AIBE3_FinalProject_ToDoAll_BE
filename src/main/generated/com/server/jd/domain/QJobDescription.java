package com.server.jd.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJobDescription is a Querydsl query type for JobDescription
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJobDescription extends EntityPathBase<JobDescription> {

    private static final long serialVersionUID = 1179370675L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJobDescription jobDescription = new QJobDescription("jobDescription");

    public final com.server.global.entity.QBaseEntity _super = new com.server.global.entity.QBaseEntity(this);

    public final NumberPath<Long> applicantCount = createNumber("applicantCount", Long.class);

    public final com.server.user.domain.QUser author;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DatePath<java.time.LocalDate> deadline = createDate("deadline", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath department = createString("department");

    public final StringPath description = createString("description");

    public final StringPath education = createString("education");

    public final StringPath experience = createString("experience");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath location = createString("location");

    public final StringPath salary = createString("salary");

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final EnumPath<JobStatus> status = createEnum("status", JobStatus.class);

    public final StringPath thumbnailUrl = createString("thumbnailUrl");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath welfare = createString("welfare");

    public final StringPath workType = createString("workType");

    public QJobDescription(String variable) {
        this(JobDescription.class, forVariable(variable), INITS);
    }

    public QJobDescription(Path<? extends JobDescription> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJobDescription(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJobDescription(PathMetadata metadata, PathInits inits) {
        this(JobDescription.class, metadata, inits);
    }

    public QJobDescription(Class<? extends JobDescription> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.author = inits.isInitialized("author") ? new com.server.user.domain.QUser(forProperty("author")) : null;
    }

}

