package com.server.search.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Id;

@Entity
@Table(name = "recommendation_reasons")
@IdClass(RecommendationKey.class)
public class RecommendationReason extends BaseEntity {
    @Id
    private Long jdId;

    @Id
    private Long resumeId;

    @Lob
    private String reason;

    public static RecommendationReason of(Long jdId, Long resumeId, String reason) {
        RecommendationReason entity = new RecommendationReason();
        entity.jdId = jdId;
        entity.resumeId = resumeId;
        entity.reason = reason;
        return entity;
    }
}
