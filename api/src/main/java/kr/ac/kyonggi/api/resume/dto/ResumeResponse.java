package kr.ac.kyonggi.api.resume.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.api.education.dto.EducationResponse;
import kr.ac.kyonggi.api.certification.dto.CertificationResponse;
import kr.ac.kyonggi.api.profile.dto.ProfileResponse;
import kr.ac.kyonggi.domain.certification.Certification;
import kr.ac.kyonggi.domain.education.Education;
import kr.ac.kyonggi.domain.resume.Resume;
import kr.ac.kyonggi.domain.resume.ResumedExperience;
import kr.ac.kyonggi.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeResponse(
        @Schema(description = "기본 프로필 정보")
        ProfileResponse basicInfo,

        @ArraySchema(
                arraySchema = @Schema(description = "AI 요약된 프로젝트 활동 목록"),
                schema = @Schema(implementation = SummarizedExperienceResponse.class)
        )
        List<SummarizedExperienceResponse> summarizedExperiences,

        @ArraySchema(
                arraySchema = @Schema(description = "학력 목록"),
                schema = @Schema(implementation = EducationResponse.class)
        )
        List<EducationResponse> educations,

        @ArraySchema(
                arraySchema = @Schema(description = "자격증 목록"),
                schema = @Schema(implementation = CertificationResponse.class)
        )
        List<CertificationResponse> certifications,

        @Schema(description = "이력서 생성 일시 (ISO 8601)", example = "2026-03-31T09:30:00", type = "string")
        LocalDateTime generatedAt
) {
    public static ResumeResponse from(User user, Resume resume, List<ResumedExperience> experiences,
                                      List<Education> educations, List<Certification> certifications) {
        return new ResumeResponse(
                ProfileResponse.from(user),
                experiences.stream().map(SummarizedExperienceResponse::from).toList(),
                educations.stream().map(EducationResponse::from).toList(),
                certifications.stream().map(CertificationResponse::from).toList(),
                resume.getGeneratedAt()
        );
    }
}
