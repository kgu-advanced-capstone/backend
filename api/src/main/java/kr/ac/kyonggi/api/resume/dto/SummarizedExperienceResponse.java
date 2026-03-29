package kr.ac.kyonggi.api.resume.dto;

import kr.ac.kyonggi.domain.resume.ResumedExperience;

import java.util.List;

public record SummarizedExperienceResponse(
        Long projectId,
        String projectTitle,
        List<String> keyPoints
) {
    public static SummarizedExperienceResponse from(ResumedExperience experience) {
        return new SummarizedExperienceResponse(
                experience.getProjectId(),
                experience.getProjectTitle(),
                experience.getKeyPoints()
        );
    }
}
