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
    public static ResumeRecommendationDto from(Resume resume, ResumeDocument doc, float score, List<String> missingSkills, String summary) {
        return new ResumeRecommendationDto(
                resume.getId(),
                resume.getName(),
                resume.getGender(),
                resume.getBirthDate(),
                Math.round(score * 1000f) / 10f, // 예시: 0.835 → 83.5으로 변환
                (int) ((1 - ((float) missingSkills.size() / doc.getSkills().size())) * 100) + "%",
                missingSkills,
                summary,
                doc.getSkills(),
                "RECOMMENDED"
        );
    }
}
