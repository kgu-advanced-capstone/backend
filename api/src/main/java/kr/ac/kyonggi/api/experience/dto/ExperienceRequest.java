package kr.ac.kyonggi.api.experience.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ExperienceRequest(
        @NotBlank(message = "내용을 입력해주세요.")
        @Schema(description = "활동 내용", example = "React Native를 사용하여 메인 화면 UI를 구현하고 API 연동을 담당했습니다.")
        String content
) {
}
