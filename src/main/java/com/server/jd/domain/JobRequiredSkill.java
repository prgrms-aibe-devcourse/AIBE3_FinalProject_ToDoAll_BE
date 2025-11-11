package com.server.jd.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_required_skills")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobRequiredSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id")
    private JobDescription job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    public static JobRequiredSkill of(JobDescription job, Skill skill) {
        JobRequiredSkill requiredSkill = new JobRequiredSkill();
        requiredSkill.job = job;
        requiredSkill.skill = skill;
        return requiredSkill;
    }
}
