package com.server.admin.dto;

import com.server.jd.domain.JobDescription;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RecentJdDto {

    private final Long id;
    private final String title;
    private final String status;
    private final LocalDateTime createdAt;

    private RecentJdDto(Long id, String title, String status, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static RecentJdDto from(JobDescription jd) {
        return new RecentJdDto(
                jd.getId(),
                jd.getTitle(),
                jd.getStatus().name(),
                jd.getCreatedAt()
        );
    }
}
