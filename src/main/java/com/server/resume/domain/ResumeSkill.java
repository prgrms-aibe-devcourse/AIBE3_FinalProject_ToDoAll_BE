package com.server.resume.domain;

import com.server.jd.domain.Skill;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "resume_skills")
public class ResumeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill; // 기술 엔티티 JD 도메인에 들어있습니다.

    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency_level")
    private ProficiencyLevel proficiencyLevel;

    public static ResumeSkill of(Resume resume, Skill skill, ProficiencyLevel proficiencyLevel) {
        ResumeSkill rs = new ResumeSkill();
        rs.resume = resume;
        rs.skill = skill;
        rs.proficiencyLevel = proficiencyLevel;
        return rs;
    }
}