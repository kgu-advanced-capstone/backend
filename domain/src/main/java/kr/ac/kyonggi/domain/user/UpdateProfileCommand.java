package kr.ac.kyonggi.domain.user;

public record UpdateProfileCommand(
        String name,
        String phone,
        String github,
        String blog,
        String profileImage
) {
}
