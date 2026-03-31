package kr.ac.kyonggi.api.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다.")
        @Schema(description = "이름 (2~50자)", example = "홍길동")
        String name,

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,

        @Size(max = 200, message = "GitHub URL은 200자 이하여야 합니다.")
        @Schema(description = "GitHub 프로필 URL", example = "https://github.com/honggildong")
        String github,

        @Size(max = 200, message = "블로그 URL은 200자 이하여야 합니다.")
        @Schema(description = "기술 블로그 URL", example = "https://velog.io/@honggildong")
        String blog,

        @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다.")
        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profile/hong.jpg")
        String profileImage
) {
}
