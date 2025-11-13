package com.server.jd.repository;

import com.server.jd.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByNameIn(Collection<String> names);
}
