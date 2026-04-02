package kr.ac.kyonggi.api.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.user.User;

import java.time.LocalDate;

public record ParticipantResponse(
        @Schema(description = "사용자 ID", example = "7")
        Long userId,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/image.png")
        String profileImage,

        @Schema(description = "GitHub URL", example = "https://github.com/example")
        String github,

        @Schema(description = "블로그 URL", example = "https://blog.example.com")
        String blog,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "프로젝트 참여일 (yyyy-MM-dd)", example = "2026-03-15", type = "string")
        LocalDate joinedAt
) {
    public static ParticipantResponse of(User user, ProjectMember member) {
        return new ParticipantResponse(
                user.getId(),
                user.getName(),
                user.getProfileImage(),
                user.getGithub(),
                user.getBlog(),
                member.getJoinedAt() != null ? member.getJoinedAt().toLocalDate() : null
        );
    }
}
