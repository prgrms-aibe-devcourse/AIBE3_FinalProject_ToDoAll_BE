package com.server.resume.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "resume_certifications")
public class ResumeCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Enumerated(EnumType.STRING)
    private ResumeCertificationType type;

    private String name;

    private String scoreOrLevel;

    public static ResumeCertification of(Resume resume,
                                         ResumeCertificationType type,
                                         String name,
                                         String scoreOrLevel) {
        ResumeCertification certification = new ResumeCertification();
        certification.resume = resume;
        certification.type = type;
        certification.name = name;
        certification.scoreOrLevel = scoreOrLevel;
        return certification;
    }
}

