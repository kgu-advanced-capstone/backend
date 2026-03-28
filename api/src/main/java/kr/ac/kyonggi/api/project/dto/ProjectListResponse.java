package kr.ac.kyonggi.api.project.dto;

import java.util.List;

public record ProjectListResponse(
        List<ProjectSummaryResponse> projects,
        long totalCount
) {}
