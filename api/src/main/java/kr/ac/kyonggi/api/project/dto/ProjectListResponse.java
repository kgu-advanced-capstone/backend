package kr.ac.kyonggi.api.project.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ProjectListResponse(
        @ArraySchema(
                arraySchema = @Schema(description = "프로젝트 목록"),
                schema = @Schema(implementation = ProjectSummaryResponse.class)
        )
        List<ProjectSummaryResponse> projects,

        @Schema(description = "전체 프로젝트 수", example = "42")
        long totalCount
) {}
