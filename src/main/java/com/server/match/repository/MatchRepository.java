package com.server.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.server.match.domain.Match;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> , MatchQueryRepository{

    boolean existsByJobDescription_IdAndResume_Id(Long jdId, Long resumeId);
    Optional<Match> findByJobDescription_IdAndResume_Id(Long jdId, Long resumeId);
    List<Match> findAllByOrderByAppliedAtDesc();
}
