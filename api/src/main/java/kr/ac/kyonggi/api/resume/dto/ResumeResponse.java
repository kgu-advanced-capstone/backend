package kr.ac.kyonggi.api.resume.dto;

import kr.ac.kyonggi.api.profile.dto.ProfileResponse;
import kr.ac.kyonggi.domain.resume.Resume;
import kr.ac.kyonggi.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeResponse(
        ProfileResponse basicInfo,
        List<SummarizedExperienceResponse> summarizedExperiences,
        LocalDateTime generatedAt
) {
    public static ResumeResponse from(User user, Resume resume) {
        List<SummarizedExperienceResponse> experiences = resume.getExperiences().stream()
                .map(SummarizedExperienceResponse::from)
                .toList();
        return new ResumeResponse(
                ProfileResponse.from(user),
                experiences,
                resume.getGeneratedAt()
        );
    }
}
