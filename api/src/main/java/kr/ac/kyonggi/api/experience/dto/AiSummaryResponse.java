package kr.ac.kyonggi.api.experience.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.experience.Experience;

public record AiSummaryResponse(
        @Schema(description = "활동 기록 고유 ID", example = "7")
        Long id,

        @Schema(description = "AI가 생성한 요약문",
                example = "UI 구현 및 REST API 연동 경험. React Native 컴포넌트 설계와 Spring Boot 백엔드 통신 구현.")
        String aiSummary
) {
    public static AiSummaryResponse from(Experience experience) {
        return new AiSummaryResponse(
                experience.getId(),
                experience.getAiSummary()
        );
    }
}
