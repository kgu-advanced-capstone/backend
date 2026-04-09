package kr.ac.kyonggi.domain.project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectData(
        Long id,
        String title,
        String description,
        String category,
        List<String> skills,
        int maxMembers,
        int currentMemberCount,
        LocalDate deadline,
        String status,
        Long authorId,
        LocalDateTime createdAt
) {
    public static ProjectData from(Project project) {
        return new ProjectData(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getCategory(),
                List.copyOf(project.getSkills()),
                project.getMaxMembers(),
                project.getCurrentMemberCount(),
                project.getDeadline(),
                project.getStatus().name(),
                project.getAuthorId(),
                project.getCreatedAt()
        );
    }
}
