package com.server.match.util;

import com.server.jd.domain.JobDescription;
import com.server.resume.domain.Resume;
import com.server.search.document.ResumeDocument;

import java.util.*;
import java.util.stream.Collectors;

public class MatchScoreCalculator {

    public static float calculateMatchScoreWithKeywords(
            JobDescription jd,
            ResumeDocument doc,
            Resume resume,
            List<String> jdKeywords
    ) {
        float skillScore = calculateSkillScore(jd, doc); // 기존 스킬 점수

        // 학력 점수 (전공 + 학점)
        float educationScore = resume.getEducations().stream()
                .filter(e -> {
                    String major = e.getMajor().toLowerCase();
                    return major.contains("컴퓨터") || major.contains("정보") || major.contains("소프트웨어");
                })
                .map(e -> e.getGpa() >= 3.0 ? 1f : 0.7f)
                .findFirst().orElse(0f);

        // 경력 점수 (JD 키워드 포함)
        float experienceScore = resume.getExperiences().stream()
                .anyMatch(e -> jdKeywords.stream().anyMatch(kw -> e.getPosition().toLowerCase().contains(kw))) ? 1f : 0f;

        // 자격증 점수
        float certificationScore = resume.getCertifications().stream()
                .anyMatch(c -> jdKeywords.stream().anyMatch(kw -> c.getName().toLowerCase().contains(kw))) ? 1f : 0f;

        // 활동 점수
        float activityScore = resume.getActivities().stream()
                .anyMatch(a -> jdKeywords.stream().anyMatch(kw -> a.getTitle().toLowerCase().contains(kw))) ? 1f : 0f;

        return (skillScore * 0.5f)
                + (educationScore * 0.15f)
                + (experienceScore * 0.15f)
                + (certificationScore * 0.10f)
                + (activityScore * 0.10f);
    }

    public static float calculateMatchScore(JobDescription jd, ResumeDocument doc, Resume resume) {
        List<String> extractedKeywords = extractKeywords(jd.getDescription());
        return calculateMatchScoreWithKeywords(jd, doc, resume, extractedKeywords);
    }

    // 스킬 점수 분리 (가중치 적용)
    private static float calculateSkillScore(JobDescription jd, ResumeDocument doc) {
        List<String> resumeSkills = doc.getSkills().stream().map(String::toLowerCase).toList();
        List<String> requiredSkills = jd.getRequiredSkillNames();
        List<String> preferredSkills = jd.getPreferredSkillNames();

        long matchedRequired = requiredSkills.stream().filter(resumeSkills::contains).count();
        long matchedPreferred = preferredSkills.stream().filter(resumeSkills::contains).count();

        float requiredScore = requiredSkills.isEmpty() ? 0 : (float) matchedRequired / requiredSkills.size();
        float preferredScore = preferredSkills.isEmpty() ? 0 : (float) matchedPreferred / preferredSkills.size();

        return (requiredScore * 0.7f) + (preferredScore * 0.3f);
    }

    // 키워드 분해 (AI 미사용 시 fallback)
    private static List<String> extractKeywords(String text) {
        if (text == null) return List.of();
        return Arrays.stream(text.toLowerCase().split("[\\s,()]+"))
                .filter(w -> w.length() > 2)
                .distinct()
                .collect(Collectors.toList());
    }

    //JD 스킬 중 이력서에 없는 스킬 목록
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
