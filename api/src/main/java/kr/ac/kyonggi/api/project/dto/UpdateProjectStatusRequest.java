package kr.ac.kyonggi.api.project.dto;

import jakarta.validation.constraints.NotNull;
import kr.ac.kyonggi.domain.project.ProjectStatus;

public record UpdateProjectStatusRequest(
        @NotNull(message = "상태를 입력해주세요.")
        ProjectStatus status
) {}
