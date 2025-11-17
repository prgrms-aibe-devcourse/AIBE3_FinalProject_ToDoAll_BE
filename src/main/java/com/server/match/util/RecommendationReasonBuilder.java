package com.server.match.util;

import com.server.search.document.ResumeDocument;

import java.util.List;
import java.util.stream.Collectors;

public class RecommendationReasonBuilder {

    public static String buildReason(String jdDescription, ResumeDocument resume) {
        String lowerDesc = jdDescription.toLowerCase();

        List<String> matchedSkills = resume.getSkills().stream()
                .map(String::toLowerCase)
                .filter(lowerDesc::contains)
                .collect(Collectors.toList());

        if (matchedSkills.isEmpty()) {
            return "공고와 기술 스택의 유사성이 낮습니다.";
        }

        return "다음 기술 경험이 공고와 일치합니다: " + String.join(", ", matchedSkills);
    }
}
