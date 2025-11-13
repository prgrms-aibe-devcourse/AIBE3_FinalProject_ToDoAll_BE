package com.server.jd.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJobPreferredSkill is a Querydsl query type for JobPreferredSkill
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJobPreferredSkill extends EntityPathBase<JobPreferredSkill> {

    private static final long serialVersionUID = -333289927L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJobPreferredSkill jobPreferredSkill = new QJobPreferredSkill("jobPreferredSkill");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QJobDescription job;

    public final QSkill skill;

    public QJobPreferredSkill(String variable) {
        this(JobPreferredSkill.class, forVariable(variable), INITS);
    }

    public QJobPreferredSkill(Path<? extends JobPreferredSkill> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJobPreferredSkill(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJobPreferredSkill(PathMetadata metadata, PathInits inits) {
        this(JobPreferredSkill.class, metadata, inits);
    }

    public QJobPreferredSkill(Class<? extends JobPreferredSkill> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.job = inits.isInitialized("job") ? new QJobDescription(forProperty("job"), inits.get("job")) : null;
        this.skill = inits.isInitialized("skill") ? new QSkill(forProperty("skill")) : null;
    }

}

