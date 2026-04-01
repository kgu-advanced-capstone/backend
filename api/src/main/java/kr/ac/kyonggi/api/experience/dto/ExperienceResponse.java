package kr.ac.kyonggi.api.experience.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.experience.Experience;

import java.time.LocalDate;

public record ExperienceResponse(
        @Schema(description = "활동 기록 고유 ID", example = "7")
        Long id,

        @Schema(description = "활동 내용 (직접 작성)", example = "React Native를 사용하여 메인 화면 UI를 구현하고 API 연동을 담당했습니다.")
        String content,

        @Schema(description = "AI 요약 (미생성 시 null)",
                example = "UI 구현 및 REST API 연동 경험. React Native 컴포넌트 설계와 Spring Boot 백엔드 통신 구현.")
        String aiSummary,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "기록 생성일 (yyyy-MM-dd)", example = "2026-03-20", type = "string")
        LocalDate createdAt
) {
    public static ExperienceResponse from(Experience experience) {
        return new ExperienceResponse(
                experience.getId(),
                experience.getContent(),
                experience.getAiSummary(),
                experience.getCreatedAt() != null
                        ? experience.getCreatedAt().toLocalDate()
                        : null
        );
    }
}
