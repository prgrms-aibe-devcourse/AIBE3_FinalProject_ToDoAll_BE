package com.server.resume.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QResumeExperience is a Querydsl query type for ResumeExperience
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeExperience extends EntityPathBase<ResumeExperience> {

    private static final long serialVersionUID = -2002879458L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QResumeExperience resumeExperience = new QResumeExperience("resumeExperience");

    public final StringPath companyName = createString("companyName");

    public final StringPath department = createString("department");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath position = createString("position");

    public final QResume resume;

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public QResumeExperience(String variable) {
        this(ResumeExperience.class, forVariable(variable), INITS);
    }

    public QResumeExperience(Path<? extends ResumeExperience> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QResumeExperience(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QResumeExperience(PathMetadata metadata, PathInits inits) {
        this(ResumeExperience.class, metadata, inits);
    }

    public QResumeExperience(Class<? extends ResumeExperience> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.resume = inits.isInitialized("resume") ? new QResume(forProperty("resume"), inits.get("resume")) : null;
    }

}

