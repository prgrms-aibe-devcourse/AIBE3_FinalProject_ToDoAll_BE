package com.server.match.util;

import com.server.jd.domain.JobDescription;
import com.server.resume.domain.Resume;
import com.server.search.document.ResumeDocument;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MatchScoreCalculator {

    public static float calculateMatchScore(JobDescription jd, ResumeDocument doc, Resume resume) {
        List<String> jdKeywords = extractKeywords(jd.getDescription());

        List<String> resumeSkills = doc.getSkills().stream().map(String::toLowerCase).toList();
        List<String> requiredSkills = jd.getRequiredSkillNames();
        List<String> preferredSkills = jd.getPreferredSkillNames();

        long matchedRequired = requiredSkills.stream().filter(resumeSkills::contains).count();
        long matchedPreferred = preferredSkills.stream().filter(resumeSkills::contains).count();

        float requiredScore = requiredSkills.isEmpty() ? 0 : (float) matchedRequired / requiredSkills.size();
        float preferredScore = preferredSkills.isEmpty() ? 0 : (float) matchedPreferred / preferredSkills.size();
        float skillScore = (requiredScore * 0.7f) + (preferredScore * 0.3f);

        // 학력 점수 (컴퓨터,정보 전공 + 학점 ≥ 3.0)
        float educationScore = resume.getEducations().stream()
                .filter(e -> e.getMajor().toLowerCase().contains("컴퓨터") || e.getMajor().toLowerCase().contains("정보"))
                .map(e -> e.getGpa() >= 3.0 ? 1f : 0.7f)
                .findFirst().orElse(0f);

        // 경험 점수 (경력 포지션에 JD 키워드 포함)
        float experienceScore = resume.getExperiences().stream()
                .anyMatch(e -> jdKeywords.stream().anyMatch(kw -> e.getPosition().toLowerCase().contains(kw))) ? 1f : 0f;

        // 자격증 점수
        float certificationScore = resume.getCertifications().stream()
                .anyMatch(c -> jdKeywords.stream().anyMatch(kw -> c.getName().toLowerCase().contains(kw))) ? 1f : 0f;

        // 활동 점수
        float activityScore = resume.getActivities().stream()
                .anyMatch(a -> jdKeywords.stream().anyMatch(kw -> a.getTitle().toLowerCase().contains(kw))) ? 1f : 0f;

        return (skillScore * 0.5f) +
                (educationScore * 0.15f) +
                (experienceScore * 0.15f) +
                (certificationScore * 0.10f) +
                (activityScore * 0.10f);
    }

    private static List<String> extractKeywords(String text) {
        if (text == null) return List.of();
        return List.of(text.toLowerCase().split("[\\s,()]+")).stream()
                .filter(w -> w.length() > 2)
                .distinct()
                .toList();
    }

    public static List<String> getMissingSkills(JobDescription jd, ResumeDocument resume) {
        List<String> requiredSkills = jd.getRequiredSkillNames().stream().map(String::toLowerCase).toList();
        List<String> preferredSkills = jd.getPreferredSkillNames().stream().map(String::toLowerCase).toList();
        List<String> resumeSkills = resume.getSkills().stream().map(String::toLowerCase).toList();

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
