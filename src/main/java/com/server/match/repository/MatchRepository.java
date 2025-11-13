package com.server.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.server.match.domain.Match;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> , MatchQueryRepository{

    boolean existsByJobDescription_IdAndResume_Id(Long jdId, Long resumeId);
}
