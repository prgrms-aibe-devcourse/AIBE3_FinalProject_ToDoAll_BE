package com.server.search.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.server.resume.domain.Resume;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@NoArgsConstructor
@Document(indexName = "resume")
public class ResumeDocument {

    @Id
    private Long id;

    private String name;
    private String gender;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate birthDate;

    @Field(type = FieldType.Long)
    private Long jdId;

    private String email;
    private String phone;
    private String address;
    private String detailAddress;

    private String resumeFileUrl;
    private String portfolioFileUrl;

    private String educationSummary;
    private String experienceSummary;
    private List<String> skills;
    private String certificationSummary;
    private String activitySummary;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String fullText;

    public static ResumeDocument of(Resume resume) {
        ResumeDocument doc = new ResumeDocument();

        doc.id = resume.getId();
        doc.name = resume.getName();
        doc.gender = resume.getGender();
        doc.birthDate = resume.getBirthDate();

        doc.jdId = resume.getJobDescription().getId();

        doc.email = resume.getEmail();
        doc.phone = resume.getPhone();
        doc.address = resume.getAddress();
        doc.detailAddress = resume.getDetailAddress();

        doc.resumeFileUrl = resume.getResumeFileUrl();
        doc.portfolioFileUrl = resume.getPortfolioFileUrl();

        doc.educationSummary = resume.getEducations().stream()
                .map(e -> e.getSchoolName() + " " + e.getMajor())
                .collect(Collectors.joining(", "));

        doc.experienceSummary = resume.getExperiences().stream()
                .map(e -> e.getCompanyName() + " " + e.getPosition())
                .collect(Collectors.joining(", "));

        doc.skills = resume.getSkills().stream()
                .map(s -> s.getSkill().getName().toLowerCase())
                .collect(Collectors.toList());

        doc.certificationSummary = resume.getCertifications().stream()
                .map(c -> c.getName())
                .collect(Collectors.joining(", "));

        doc.activitySummary = resume.getActivities().stream()
                .map(a -> a.getTitle())
                .collect(Collectors.joining(", "));

        doc.fullText = buildFullText(resume);

        return doc;
    }


    // 전체 텍스트 생성 (추천 점수 계산용)
    private static String buildFullText(Resume resume) {
        StringBuilder sb = new StringBuilder();

        resume.getEducations().forEach(e ->
                sb.append(e.getSchoolName()).append(" ").append(e.getMajor()).append(" ")
        );
        resume.getExperiences().forEach(e ->
                sb.append(e.getCompanyName()).append(" ").append(e.getPosition()).append(" ")
        );
        resume.getSkills().forEach(s ->
                sb.append(s.getSkill().getName()).append(" ")
        );
        resume.getCertifications().forEach(c ->
                sb.append(c.getName()).append(" ")
        );
        resume.getActivities().forEach(a ->
                sb.append(a.getTitle()).append(" ")
        );

        return sb.toString();
    }

    public Resume toEntity() {
        return Resume.createFromDocument(this);
    }
}
