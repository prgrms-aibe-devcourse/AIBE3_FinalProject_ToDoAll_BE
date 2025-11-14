package com.server.jd.dto;

import com.server.jd.domain.Skill;

public record SkillResponseDto (
        Long id,
        String name
) {
    public static SkillResponseDto from(Skill skill) {
        return new SkillResponseDto(
                skill.getId(),
                skill.getName()
        );
    }
}
