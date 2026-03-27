package kr.ac.kyonggi.api.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    public static MyProjectResponse from(ProjectMember member, Long currentUserId, long memberCount) {
        return new MyProjectResponse(
                ProjectDetailResponse.from(member.getProject(), memberCount),
                member.getJoinedAt() != null ? member.getJoinedAt().toLocalDate() : null,
                member.getProject().getStatus(),
                member.getProject().isAuthor(currentUserId)
        );
    }
}
