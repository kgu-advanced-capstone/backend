package kr.ac.kyonggi.domain.experience;

import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.user.User;

public record ExperienceCreateCommand(
        User user,
        Project project,
        String content
) {
}
