package kr.ac.kyonggi.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
        String password,

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
        String name

) {}
