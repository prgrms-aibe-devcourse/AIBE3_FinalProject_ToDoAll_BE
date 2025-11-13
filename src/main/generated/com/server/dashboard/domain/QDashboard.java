package com.server.dashboard.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDashboard is a Querydsl query type for Dashboard
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDashboard extends EntityPathBase<Dashboard> {

    private static final long serialVersionUID = 494109250L;

    public static final QDashboard dashboard = new QDashboard("dashboard");

    public final com.server.global.entity.QBaseEntity _super = new com.server.global.entity.QBaseEntity(this);

    public final NumberPath<Integer> completedHires = createNumber("completedHires", Integer.class);

    public final NumberPath<Integer> completedInterviews = createNumber("completedInterviews", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> totalActiveJobs = createNumber("totalActiveJobs", Integer.class);

    public final NumberPath<Integer> totalActiveResumes = createNumber("totalActiveResumes", Integer.class);

    public final NumberPath<Integer> totalInterviews = createNumber("totalInterviews", Integer.class);

    public final NumberPath<Integer> totalJobs = createNumber("totalJobs", Integer.class);

    public final NumberPath<Integer> totalMatches = createNumber("totalMatches", Integer.class);

    public final NumberPath<Integer> totalResumes = createNumber("totalResumes", Integer.class);

    public final NumberPath<Integer> upcomingInterviews = createNumber("upcomingInterviews", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QDashboard(String variable) {
        super(Dashboard.class, forVariable(variable));
    }

    public QDashboard(Path<? extends Dashboard> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDashboard(PathMetadata metadata) {
        super(Dashboard.class, metadata);
    }

}

