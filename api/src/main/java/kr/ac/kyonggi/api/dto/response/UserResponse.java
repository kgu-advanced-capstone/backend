package kr.ac.kyonggi.api.dto.response;

import kr.ac.kyonggi.domain.entity.User;

public record UserResponse(Long id, String email, String name, String profileImage) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getProfileImage());
    }
}
