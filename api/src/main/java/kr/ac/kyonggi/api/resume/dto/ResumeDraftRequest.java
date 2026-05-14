package kr.ac.kyonggi.api.resume.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResumeDraftRequest(
        @Schema(description = "자기소개서 제목", example = "자기소개서")
        String coverLetterTitle,

        @Schema(description = "자기소개서 본문", example = "지원 동기와 강점을 작성한 내용")
        String coverLetterContent
) {
}