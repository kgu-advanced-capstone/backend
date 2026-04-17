package kr.ac.kyonggi.api.certification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CertificationRequest(

        @NotBlank(message = "자격증명을 입력해주세요.")
        @Schema(description = "자격증명", example = "정보처리기사")
        String name,

        @Schema(description = "발급기관", example = "한국산업인력공단")
        String issuingOrganization,

        @NotNull(message = "취득일을 입력해주세요.")
        @Schema(description = "취득일 (yyyy-MM-dd)", example = "2023-11-15", type = "string")
        LocalDate issuedDate

) {}
