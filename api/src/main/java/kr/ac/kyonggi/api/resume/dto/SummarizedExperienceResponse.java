package kr.ac.kyonggi.api.resume.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.resume.ResumedExperience;

import java.util.List;

public record SummarizedExperienceResponse(
        @Schema(description = "프로젝트 고유 ID", example = "42")
        Long projectId,

        @Schema(description = "프로젝트 제목", example = "스마트 캠퍼스 앱 개발")
        String projectTitle,

        @ArraySchema(
                arraySchema = @Schema(description = "AI 요약 핵심 포인트 목록"),
                schema = @Schema(type = "string", example = "REST API 연동 경험")
        )
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
