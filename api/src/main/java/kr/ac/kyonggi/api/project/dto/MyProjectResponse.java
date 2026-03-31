package kr.ac.kyonggi.api.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectStatus;

import java.time.LocalDate;

public record MyProjectResponse(
        ProjectDetailResponse project,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate joinedAt,
        ProjectStatus status,
        boolean isOwner
) {
    public static MyProjectResponse from(Project project, ProjectMember member, Long currentUserId, long memberCount, String authorName) {
        return new MyProjectResponse(
                ProjectDetailResponse.from(project, memberCount, authorName),
                member.getJoinedAt() != null ? member.getJoinedAt().toLocalDate() : null,
                project.getStatus(),
                project.isAuthor(currentUserId)
        );
    }
}
