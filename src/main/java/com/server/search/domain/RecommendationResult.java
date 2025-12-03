package com.server.search.domain;

import com.server.global.exception.ApplicationException;
import com.server.search.exception.SearchErrorCase;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "recommendation_results")
@IdClass(RecommendationKey.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendationResult {

    @Id
    @Column(name = "jd_id")
    private Long jdId;

    @Id
    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "match_score", nullable = false)
    private Float matchScore;

    @Column(name = "skill_match_rate", nullable = false)
    private String skillMatchRate;

    @Lob
    @Column(name = "summary", columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Lob
    @Column(name = "recommendation_reason", columnDefinition = "TEXT", nullable = false)
    private String recommendationReason;

    @ElementCollection
    @CollectionTable(
            name = "recommendation_missing_skills",
            joinColumns = {
                    @JoinColumn(name = "jd_id", referencedColumnName = "jd_id"),
                    @JoinColumn(name = "resume_id", referencedColumnName = "resume_id")
            }
    )
    @Column(name = "skill")
    private List<String> missingSkills;

    public static RecommendationResult of(
            Long jdId,
            Long resumeId,
            Float matchScore,
            String skillMatchRate,
            String summary,
            String recommendationReason,
            List<String> missingSkills
    ) {
        if (jdId == null || resumeId == null) {
            throw new ApplicationException(SearchErrorCase.INVALID_RECOMMENDATION_ID);
        }
        if (matchScore == null || matchScore < 0 || matchScore > 100) {
            throw new ApplicationException(SearchErrorCase.INVALID_MATCH_SCORE);
        }
        if (summary == null || summary.isBlank()) {
            throw new ApplicationException(SearchErrorCase.SUMMARY_REQUIRED);
        }
        if (recommendationReason == null || recommendationReason.isBlank()) {
            throw new ApplicationException(SearchErrorCase.RECOMMENDATION_REASON_REQUIRED);
        }

        RecommendationResult result = new RecommendationResult();
        result.jdId = jdId;
        result.resumeId = resumeId;
        result.matchScore = matchScore;
        result.skillMatchRate = skillMatchRate;
        result.summary = summary;
        result.recommendationReason = recommendationReason;
        result.missingSkills = missingSkills;

        return result;
    }
}
