package kr.ac.kyonggi.api.experience.dto;

import kr.ac.kyonggi.domain.experience.AiSummaryStatus;
import kr.ac.kyonggi.domain.experience.Experience;

public record AiSummaryStatusResponse(
        Long id,
        AiSummaryStatus status,
        String aiSummary
) {
    public static AiSummaryStatusResponse from(Experience experience) {
        return new AiSummaryStatusResponse(
                experience.getId(),
                experience.getAiSummaryStatus(),
                experience.getAiSummary()
        );
    }
}
