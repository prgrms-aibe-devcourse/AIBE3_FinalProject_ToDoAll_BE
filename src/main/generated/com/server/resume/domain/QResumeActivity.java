package com.server.resume.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QResumeActivity is a Querydsl query type for ResumeActivity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeActivity extends EntityPathBase<ResumeActivity> {

    private static final long serialVersionUID = -933940541L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QResumeActivity resumeActivity = new QResumeActivity("resumeActivity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath organization = createString("organization");

    public final QResume resume;

    public final StringPath title = createString("title");

    public final EnumPath<ResumeActivityType> type = createEnum("type", ResumeActivityType.class);

    public QResumeActivity(String variable) {
        this(ResumeActivity.class, forVariable(variable), INITS);
    }

    public QResumeActivity(Path<? extends ResumeActivity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QResumeActivity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QResumeActivity(PathMetadata metadata, PathInits inits) {
        this(ResumeActivity.class, metadata, inits);
    }

    public QResumeActivity(Class<? extends ResumeActivity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.resume = inits.isInitialized("resume") ? new QResume(forProperty("resume"), inits.get("resume")) : null;
    }

}

