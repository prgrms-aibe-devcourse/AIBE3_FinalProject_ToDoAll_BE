package com.server.resume.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QResumeEducation is a Querydsl query type for ResumeEducation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeEducation extends EntityPathBase<ResumeEducation> {

    private static final long serialVersionUID = 617225844L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QResumeEducation resumeEducation = new QResumeEducation("resumeEducation");

    public final DatePath<java.time.LocalDate> admissionDate = createDate("admissionDate", java.time.LocalDate.class);

    public final EnumPath<AttendanceType> attendanceType = createEnum("attendanceType", AttendanceType.class);

    public final EnumPath<EducationLevel> educationLevel = createEnum("educationLevel", EducationLevel.class);

    public final NumberPath<Double> gpa = createNumber("gpa", Double.class);

    public final NumberPath<Double> gpaScale = createNumber("gpaScale", Double.class);

    public final DatePath<java.time.LocalDate> graduationDate = createDate("graduationDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isGraduated = createBoolean("isGraduated");

    public final StringPath major = createString("major");

    public final QResume resume;

    public final StringPath schoolName = createString("schoolName");

    public QResumeEducation(String variable) {
        this(ResumeEducation.class, forVariable(variable), INITS);
    }

    public QResumeEducation(Path<? extends ResumeEducation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QResumeEducation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QResumeEducation(PathMetadata metadata, PathInits inits) {
        this(ResumeEducation.class, metadata, inits);
    }

    public QResumeEducation(Class<? extends ResumeEducation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.resume = inits.isInitialized("resume") ? new QResume(forProperty("resume"), inits.get("resume")) : null;
    }

}

