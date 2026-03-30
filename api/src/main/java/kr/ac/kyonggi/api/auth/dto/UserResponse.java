package kr.ac.kyonggi.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.user.User;

public record UserResponse(
        @Schema(description = "사용자 고유 ID", example = "42")
        Long id,

        @Schema(description = "사용자 이메일", example = "hong@kyonggi.ac.kr")
        String email,

        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "프로필 이미지 URL (미설정 시 null)", example = "https://cdn.example.com/profile/hong.jpg")
        String profileImage
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getName(), user.getProfileImage());
    }
}
