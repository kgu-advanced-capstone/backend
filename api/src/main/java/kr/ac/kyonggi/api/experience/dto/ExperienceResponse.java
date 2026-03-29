package kr.ac.kyonggi.api.experience.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.ac.kyonggi.domain.experience.Experience;

import java.time.LocalDate;

public record ExperienceResponse(
        Long id,
        String content,
        String aiSummary,
        @JsonFormat(pattern = "yyyy-MM-dd")
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
