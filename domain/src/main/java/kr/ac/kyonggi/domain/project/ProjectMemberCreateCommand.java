package kr.ac.kyonggi.domain.project;

import kr.ac.kyonggi.domain.user.User;

public record ProjectMemberCreateCommand(
        Project project,
        User user
) {

}
