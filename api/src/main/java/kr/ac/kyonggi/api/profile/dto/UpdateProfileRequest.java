package kr.ac.kyonggi.api.profile.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 2, max = 50, message = "이름은 2~50자여야 합니다.")
        String name,

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
        String phone,

        @Size(max = 200, message = "GitHub URL은 200자 이하여야 합니다.")
        String github,

        @Size(max = 200, message = "블로그 URL은 200자 이하여야 합니다.")
        String blog,

        @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다.")
        String profileImage
) {
}
