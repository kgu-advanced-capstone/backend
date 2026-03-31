package kr.ac.kyonggi.api.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.ac.kyonggi.domain.project.Project;

import java.time.LocalDate;
import java.util.List;

public record ProjectDetailResponse(
        Long id,
        String title,
        String description,
        String category,
        List<String> skills,
        int currentMembers,
        int maxMembers,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate deadline,
        String author,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate createdAt
) {
    public static ProjectDetailResponse from(Project project, long memberCount, String authorName) {
        return new ProjectDetailResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getCategory(),
                project.getSkills(),
                (int) memberCount,
                project.getMaxMembers(),
                project.getDeadline(),
                authorName,
                project.getCreatedAt() != null ? project.getCreatedAt().toLocalDate() : null
        );
    }
}
