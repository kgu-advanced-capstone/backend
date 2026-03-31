package kr.ac.kyonggi.domain.project;

public record ProjectMemberCreateCommand(
        Long projectId,
        Long userId
) {

}
