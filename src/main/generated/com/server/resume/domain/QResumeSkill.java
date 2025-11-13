package com.server.resume.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QResumeSkill is a Querydsl query type for ResumeSkill
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeSkill extends EntityPathBase<ResumeSkill> {

    private static final long serialVersionUID = 1523107613L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QResumeSkill resumeSkill = new QResumeSkill("resumeSkill");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<ProficiencyLevel> proficiencyLevel = createEnum("proficiencyLevel", ProficiencyLevel.class);

    public final QResume resume;

    public final com.server.jd.domain.QSkill skill;

    public QResumeSkill(String variable) {
        this(ResumeSkill.class, forVariable(variable), INITS);
    }

    public QResumeSkill(Path<? extends ResumeSkill> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QResumeSkill(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QResumeSkill(PathMetadata metadata, PathInits inits) {
        this(ResumeSkill.class, metadata, inits);
    }

    public QResumeSkill(Class<? extends ResumeSkill> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.resume = inits.isInitialized("resume") ? new QResume(forProperty("resume"), inits.get("resume")) : null;
        this.skill = inits.isInitialized("skill") ? new com.server.jd.domain.QSkill(forProperty("skill")) : null;
    }

}

