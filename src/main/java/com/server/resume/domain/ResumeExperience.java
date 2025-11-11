package com.server.resume.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "resume_experiences")
public class ResumeExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "company_name")
    private String companyName;

    private String department;

    private String position;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public static ResumeExperience of(Resume resume,
                                      String companyName,
                                      String department,
                                      String position,
                                      LocalDate startDate,
                                      LocalDate endDate) {
        ResumeExperience exp = new ResumeExperience();
        exp.resume = resume;
        exp.companyName = companyName;
        exp.department = department;
        exp.position = position;
        exp.startDate = startDate;
        exp.endDate = endDate;
        return exp;
    }
}
