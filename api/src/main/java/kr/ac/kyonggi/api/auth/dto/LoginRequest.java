package kr.ac.kyonggi.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "이메일을 입력해주세요.")
        @Schema(description = "사용자 이메일", example = "hong@kyonggi.ac.kr")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Schema(description = "비밀번호 (8자 이상)", example = "password123!")
        String password

) {}
