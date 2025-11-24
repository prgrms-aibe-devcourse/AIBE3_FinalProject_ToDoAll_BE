package com.server.match.util;

import com.server.jd.domain.JobDescription;
import com.server.search.document.ResumeDocument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        List<String> requiredSkills = jd.getRequiredSkillNames().stream()
                .map(String::toLowerCase)
                .toList();
        List<String> preferredSkills = jd.getPreferredSkillNames().stream()
                .map(String::toLowerCase)
                .toList();
        List<String> resumeSkills = resume.getSkills().stream()
                .map(String::toLowerCase)
                .toList();

        Set<String> missing = new HashSet<>();
        for (String skill : requiredSkills) {
            if (!resumeSkills.contains(skill)) missing.add(skill);
        }
        for (String skill : preferredSkills) {
            if (!resumeSkills.contains(skill)) missing.add(skill);
        }

        return new ArrayList<>(missing);
    }
}