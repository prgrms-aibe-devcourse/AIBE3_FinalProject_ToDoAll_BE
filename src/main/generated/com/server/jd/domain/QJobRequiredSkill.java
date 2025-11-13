package com.server.jd.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJobRequiredSkill is a Querydsl query type for JobRequiredSkill
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJobRequiredSkill extends EntityPathBase<JobRequiredSkill> {

    private static final long serialVersionUID = -15660823L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJobRequiredSkill jobRequiredSkill = new QJobRequiredSkill("jobRequiredSkill");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QJobDescription job;

    public final QSkill skill;

    public QJobRequiredSkill(String variable) {
        this(JobRequiredSkill.class, forVariable(variable), INITS);
    }

    public QJobRequiredSkill(Path<? extends JobRequiredSkill> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJobRequiredSkill(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJobRequiredSkill(PathMetadata metadata, PathInits inits) {
        this(JobRequiredSkill.class, metadata, inits);
    }

    public QJobRequiredSkill(Class<? extends JobRequiredSkill> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.job = inits.isInitialized("job") ? new QJobDescription(forProperty("job"), inits.get("job")) : null;
        this.skill = inits.isInitialized("skill") ? new QSkill(forProperty("skill")) : null;
    }

}

