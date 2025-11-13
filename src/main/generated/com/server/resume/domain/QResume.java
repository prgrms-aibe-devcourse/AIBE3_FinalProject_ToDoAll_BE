package com.server.resume.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QResume is a Querydsl query type for Resume
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResume extends EntityPathBase<Resume> {

    private static final long serialVersionUID = -1262637164L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QResume resume = new QResume("resume");

    public final com.server.global.entity.QBaseEntity _super = new com.server.global.entity.QBaseEntity(this);

    public final ListPath<ResumeActivity, QResumeActivity> activities = this.<ResumeActivity, QResumeActivity>createList("activities", ResumeActivity.class, QResumeActivity.class, PathInits.DIRECT2);

    public final StringPath address = createString("address");

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    public final ListPath<ResumeCertification, QResumeCertification> certifications = this.<ResumeCertification, QResumeCertification>createList("certifications", ResumeCertification.class, QResumeCertification.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath detailAddress = createString("detailAddress");

    public final ListPath<ResumeEducation, QResumeEducation> educations = this.<ResumeEducation, QResumeEducation>createList("educations", ResumeEducation.class, QResumeEducation.class, PathInits.DIRECT2);

    public final StringPath email = createString("email");

    public final ListPath<ResumeExperience, QResumeExperience> experiences = this.<ResumeExperience, QResumeExperience>createList("experiences", ResumeExperience.class, QResumeExperience.class, PathInits.DIRECT2);

    public final StringPath gender = createString("gender");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.server.jd.domain.QJobDescription jobDescription;

    public final StringPath name = createString("name");

    public final StringPath phone = createString("phone");

    public final StringPath portfolioFileUrl = createString("portfolioFileUrl");

    public final StringPath resumeFileUrl = createString("resumeFileUrl");

    public final ListPath<ResumeSkill, QResumeSkill> skills = this.<ResumeSkill, QResumeSkill>createList("skills", ResumeSkill.class, QResumeSkill.class, PathInits.DIRECT2);

    public final EnumPath<ResumeStatus> status = createEnum("status", ResumeStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QResume(String variable) {
        this(Resume.class, forVariable(variable), INITS);
    }

    public QResume(Path<? extends Resume> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QResume(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QResume(PathMetadata metadata, PathInits inits) {
        this(Resume.class, metadata, inits);
    }

    public QResume(Class<? extends Resume> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.jobDescription = inits.isInitialized("jobDescription") ? new com.server.jd.domain.QJobDescription(forProperty("jobDescription"), inits.get("jobDescription")) : null;
    }

}

