package kr.ac.kyonggi.api.project.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.ac.kyonggi.domain.project.Project;

import java.time.LocalDate;
import java.util.List;

public record ProjectSummaryResponse(
        Long id,
        String title,
        String category,
        List<String> skills,
        int currentMembers,
        int maxMembers,
        String author,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate createdAt
) {
    public static ProjectSummaryResponse from(Project project, long memberCount) {
        return new ProjectSummaryResponse(
                project.getId(),
                project.getTitle(),
                project.getCategory(),
                project.getSkills(),
                (int) memberCount,
                project.getMaxMembers(),
                project.getAuthor().getName(),
                project.getCreatedAt() != null ? project.getCreatedAt().toLocalDate() : null
        );
    }
}
