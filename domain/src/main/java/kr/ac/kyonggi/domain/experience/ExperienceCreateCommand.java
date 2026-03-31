package kr.ac.kyonggi.domain.experience;

public record ExperienceCreateCommand(
        Long userId,
        Long projectId,
        String content
) {
}
