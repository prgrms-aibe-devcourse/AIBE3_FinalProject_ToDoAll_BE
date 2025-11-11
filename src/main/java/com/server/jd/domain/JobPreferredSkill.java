package com.server.jd.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_preferred_skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPreferredSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id")
    private JobDescription job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    public static JobPreferredSkill of(JobDescription job, Skill skill) {
        JobPreferredSkill preferredSkill = new JobPreferredSkill();
        preferredSkill.job = job;
        preferredSkill.skill = skill;
        return preferredSkill;
    }
}
