package com.server.match.util;

import com.server.jd.domain.JobDescription;
import com.server.search.document.ResumeDocument;

import java.util.ArrayList;
import java.util.List;

public class MatchScoreCalculator {

    public static float calculateMatchScore(JobDescription jd, ResumeDocument resume) {
        List<String> requiredSkills = jd.getRequiredSkillNames();
        List<String> preferredSkills = jd.getPreferredSkillNames();
        List<String> resumeSkills = resume.getSkills().stream()
                .map(String::toLowerCase)
                .toList();

        long matchedRequired = requiredSkills.stream().filter(resumeSkills::contains).count();
        long matchedPreferred = preferredSkills.stream().filter(resumeSkills::contains).count();

        float requiredScore = requiredSkills.isEmpty() ? 0 : (float) matchedRequired / requiredSkills.size();
        float preferredScore = preferredSkills.isEmpty() ? 0 : (float) matchedPreferred / preferredSkills.size();

        // 필수 70%, 우대 30% 가중치 설정
        return (requiredScore * 0.7f) + (preferredScore * 0.3f);
    }

    public static List<String> getMissingSkills(JobDescription jd, ResumeDocument resume) {
        List<String> requiredSkills = jd.getRequiredSkillNames();
        List<String> resumeSkills = resume.getSkills().stream()
                .map(String::toLowerCase)
                .toList();

        List<String> missing = new ArrayList<>();
        for (String required : requiredSkills) {
            if (!resumeSkills.contains(required)) {
                missing.add(required);
            }
        }

        return missing;
    }
}