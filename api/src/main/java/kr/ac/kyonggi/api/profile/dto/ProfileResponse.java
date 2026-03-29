package kr.ac.kyonggi.api.profile.dto;

import kr.ac.kyonggi.domain.user.User;

public record ProfileResponse(
        String name,
        String email,
        String phone,
        String github,
        String blog,
        String profileImage
) {
    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getGithub(),
                user.getBlog(),
                user.getProfileImage()
        );
    }
}
