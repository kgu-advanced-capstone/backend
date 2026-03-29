package kr.ac.kyonggi.api.experience.dto;

import jakarta.validation.constraints.NotBlank;

public record ExperienceRequest(
        @NotBlank(message = "내용을 입력해주세요.")
        String content
) {
}
