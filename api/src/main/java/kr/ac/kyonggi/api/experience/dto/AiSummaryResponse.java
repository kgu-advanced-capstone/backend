package kr.ac.kyonggi.api.experience.dto;

import kr.ac.kyonggi.domain.experience.Experience;

public record AiSummaryResponse(
        Long id,
        String aiSummary
) {
    public static AiSummaryResponse from(Experience experience) {
        return new AiSummaryResponse(
                experience.getId(),
                experience.getAiSummary()
        );
    }
}
