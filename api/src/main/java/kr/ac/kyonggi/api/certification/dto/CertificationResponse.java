package kr.ac.kyonggi.api.certification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.certification.Certification;

import java.time.LocalDate;

public record CertificationResponse(

        @Schema(description = "자격증 고유 ID", example = "1")
        Long id,

        @Schema(description = "자격증명", example = "정보처리기사")
        String name,

        @Schema(description = "발급기관", example = "한국산업인력공단")
        String issuingOrganization,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "취득일 (yyyy-MM-dd)", example = "2023-11-15", type = "string")
        LocalDate issuedDate

) {
    public static CertificationResponse from(Certification certification) {
        return new CertificationResponse(
                certification.getId(),
                certification.getName(),
                certification.getIssuingOrganization(),
                certification.getIssuedDate()
        );
    }
}
