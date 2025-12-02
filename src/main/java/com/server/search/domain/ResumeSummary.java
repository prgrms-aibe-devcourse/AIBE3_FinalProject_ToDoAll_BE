package com.server.search.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resume_summaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeSummary extends BaseEntity {
    @Id
    private Long resumeId;

    @Lob
    private String summary;

    public static ResumeSummary of(Long resumeId, String summary) {
        ResumeSummary entity = new ResumeSummary();
        entity.resumeId = resumeId;
        entity.summary = summary;
        return entity;
    }
}
