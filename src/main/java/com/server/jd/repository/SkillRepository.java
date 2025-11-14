package com.server.jd.repository;

import com.server.jd.domain.Skill;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);
    List<Skill> findByNameIn(Collection<String> names);

}
