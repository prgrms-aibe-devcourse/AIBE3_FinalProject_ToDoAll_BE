package com.server.resume.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QResumeCertification is a Querydsl query type for ResumeCertification
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeCertification extends EntityPathBase<ResumeCertification> {

    private static final long serialVersionUID = -526137978L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QResumeCertification resumeCertification = new QResumeCertification("resumeCertification");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final QResume resume;

    public final StringPath scoreOrLevel = createString("scoreOrLevel");

    public final EnumPath<ResumeCertificationType> type = createEnum("type", ResumeCertificationType.class);

    public QResumeCertification(String variable) {
        this(ResumeCertification.class, forVariable(variable), INITS);
    }

    public QResumeCertification(Path<? extends ResumeCertification> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QResumeCertification(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QResumeCertification(PathMetadata metadata, PathInits inits) {
        this(ResumeCertification.class, metadata, inits);
    }

    public QResumeCertification(Class<? extends ResumeCertification> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.resume = inits.isInitialized("resume") ? new QResume(forProperty("resume"), inits.get("resume")) : null;
    }

}

