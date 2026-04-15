package kr.ac.kyonggi.api.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Schema(description = "사용자 이메일", example = "hong@kyonggi.ac.kr")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
        @Schema(description = "비밀번호 (8~100자)", example = "password123!")
        String password,

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
        @Schema(description = "이름 (2~50자)", example = "홍길동")
        String name,

        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone

) {}
