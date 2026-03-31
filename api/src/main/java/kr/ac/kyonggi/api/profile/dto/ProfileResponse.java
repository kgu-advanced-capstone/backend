package kr.ac.kyonggi.api.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.user.User;

public record ProfileResponse(
        @Schema(description = "사용자 이름", example = "홍길동")
        String name,

        @Schema(description = "사용자 이메일", example = "hong@kyonggi.ac.kr")
        String email,

        @Schema(description = "전화번호 (미설정 시 null)", example = "010-1234-5678")
        String phone,

        @Schema(description = "GitHub 프로필 URL (미설정 시 null)", example = "https://github.com/honggildong")
        String github,

        @Schema(description = "기술 블로그 URL (미설정 시 null)", example = "https://velog.io/@honggildong")
        String blog,

        @Schema(description = "프로필 이미지 URL (미설정 시 null)", example = "https://cdn.example.com/profile/hong.jpg")
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
