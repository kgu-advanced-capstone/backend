package kr.ac.kyonggi.api.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.project.Project;

import java.time.LocalDate;
import java.util.List;

public record ProjectDetailResponse(
        @Schema(description = "프로젝트 고유 ID", example = "42")
        Long id,

        @Schema(description = "프로젝트 제목", example = "스마트 캠퍼스 앱 개발")
        String title,

        @Schema(description = "프로젝트 설명", example = "React Native와 Spring Boot를 활용한 캠퍼스 정보 앱")
        String description,

        @Schema(description = "카테고리", example = "모바일")
        String category,

        @ArraySchema(
                arraySchema = @Schema(description = "사용 기술 스택 목록"),
                schema = @Schema(type = "string", example = "React Native")
        )
        List<String> skills,

        @Schema(description = "현재 팀원 수", example = "2")
        int currentMembers,

        @Schema(description = "최대 팀원 수", example = "4")
        int maxMembers,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "모집 마감일 (yyyy-MM-dd)", example = "2026-06-30", type = "string")
        LocalDate deadline,

        @Schema(description = "프로젝트 생성자 이름", example = "홍길동")
        String author,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "프로젝트 생성일 (yyyy-MM-dd)", example = "2026-03-01", type = "string")
        LocalDate createdAt
) {
    public static ProjectDetailResponse from(Project project, long memberCount) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getCategory(),
                project.getSkills(),
                (int) memberCount,
                project.getMaxMembers(),
                project.getDeadline(),
                project.getAuthor().getName(),
                project.getCreatedAt() != null ? project.getCreatedAt().toLocalDate() : null
        );
    }
}
