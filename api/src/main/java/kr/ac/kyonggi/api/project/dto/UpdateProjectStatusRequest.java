package kr.ac.kyonggi.api.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import kr.ac.kyonggi.domain.project.ProjectStatus;

public record UpdateProjectStatusRequest(
        @NotNull(message = "상태를 입력해주세요.")
        @Schema(description = "변경할 프로젝트 상태", example = "in-progress")
        ProjectStatus status
) {}
