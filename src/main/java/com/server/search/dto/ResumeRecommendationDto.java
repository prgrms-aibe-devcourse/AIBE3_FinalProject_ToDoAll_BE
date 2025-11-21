package com.server.search.dto;

import com.server.resume.domain.Resume;
import com.server.search.document.ResumeDocument;

import java.time.LocalDate;
import java.util.List;

public record ResumeRecommendationDto(
        Long resumeId,
        String name,
        String gender,
        LocalDate birthDate,
        float matchScore,
        String skillMatchRate,
        List<String> missingSkills,
        String summary,
        List<String> skills,
        String status
) {
    public static ResumeRecommendationDto from(
            Resume resume,
            ResumeDocument doc,
            float score,
            List<String> missingSkills,
            String summary
    ) {
        int totalRequired = missingSkills.size() + (int) doc.getSkills().stream()
                .filter(skill -> !missingSkills.contains(skill))
                .count();

        float percentage = 0f;
        if (totalRequired > 0) {
            percentage = (float) (totalRequired - missingSkills.size()) / totalRequired;
        }

        String skillMatchRate = Math.round(percentage * 100) + "%";

        return new ResumeRecommendationDto(
                resume.getId(),
                resume.getName(),
                resume.getGender(),
                resume.getBirthDate(),
                Math.round(score * 1000f) / 10f,
                skillMatchRate,
                missingSkills,
                summary,
                doc.getSkills(),
                "RECOMMENDED"
        );
    }
}
