package com.server.match.util;

import com.server.search.document.ResumeDocument;

import java.util.List;

public class MatchScoreCalculator {

    public static float calculateMatchScore(String jdDescription, ResumeDocument resume) {
        String lowerDesc = jdDescription.toLowerCase();

        List<String> skills = resume.getSkills();
        if (skills == null || skills.isEmpty()) {
            return 0.0f;
        }

        long matchCount = skills.stream()
                .map(String::toLowerCase)
                .filter(lowerDesc::contains)
                .count();

        return (float) matchCount / skills.size();  // 예시: 3개 중 2개 일치하면 → 0.66
    }
}