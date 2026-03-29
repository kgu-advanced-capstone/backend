package kr.ac.kyonggi.api.auth.dto;

import kr.ac.kyonggi.domain.user.User;

public record UserResponse(Long id, String email, String name, String profileImage) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getProfileImage());
    }
}
