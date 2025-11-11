package com.server.resume.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "resume_activities")
public class ResumeActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    private String title;

    @Enumerated(EnumType.STRING)
    private ResumeActivityType type;

    private String organization;

    private LocalDate startDate;

    private LocalDate endDate;

    public static ResumeActivity of(Resume resume,
                                    String title,
                                    ResumeActivityType type,
                                    String organization,
                                    LocalDate startDate,
                                    LocalDate endDate) {
        ResumeActivity activity = new ResumeActivity();
        activity.resume = resume;
        activity.title = title;
        activity.type = type;
        activity.organization = organization;
        activity.startDate = startDate;
        activity.endDate = endDate;
        return activity;
    }
}
