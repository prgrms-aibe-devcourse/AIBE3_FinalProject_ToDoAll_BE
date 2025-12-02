package com.server.search.domain;

import com.server.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "resume_summaries")
public class ResumeSummary extends BaseEntity {
    @Id
    private Long resumeId;

    @Lob
    private String summary;
}
