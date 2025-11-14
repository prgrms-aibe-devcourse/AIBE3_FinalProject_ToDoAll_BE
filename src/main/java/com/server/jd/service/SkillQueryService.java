package com.server.jd.service;

import com.server.jd.domain.Skill;
import com.server.jd.dto.SkillResponseDto;
import com.server.jd.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillQueryService {
    private final SkillRepository skillRepository;

    @Transactional(readOnly = true)
    public List<SkillResponseDto> getSkills() {
        List<Skill> skills = skillRepository.findAllByOrderByNameAsc();
        return skills.stream()
                .map(SkillResponseDto::from)
                .toList();
    }
}
