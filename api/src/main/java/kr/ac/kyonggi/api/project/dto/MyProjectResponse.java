package kr.ac.kyonggi.api.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectStatus;

import java.time.LocalDate;

public record MyProjectResponse(
        @Schema(description = "프로젝트 상세 정보")
        ProjectDetailResponse project,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "팀 합류일 (yyyy-MM-dd)", example = "2026-03-15", type = "string")
        LocalDate joinedAt,

        @Schema(description = "프로젝트 상태", example = "recruiting")
        ProjectStatus status,

        @Schema(description = "프로젝트 생성자 여부", example = "true")
        boolean isOwner
) {
    public static MyProjectResponse from(
            Project project, ProjectMember member, Long currentUserId,
            long memberCount, String authorName) {
        return new MyProjectResponse(
                ProjectDetailResponse.from(project, memberCount, authorName),
                member.getJoinedAt() != null ? member.getJoinedAt().toLocalDate() : null,
                project.getStatus(),
                project.isAuthor(currentUserId)
        );
    }
}
