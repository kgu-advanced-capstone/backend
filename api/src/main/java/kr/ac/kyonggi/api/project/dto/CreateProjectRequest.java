package kr.ac.kyonggi.api.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public record CreateProjectRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        @Schema(description = "프로젝트 제목", example = "스마트 캠퍼스 앱 개발")
        String title,

        @Schema(description = "프로젝트 설명", example = "React Native와 Spring Boot를 활용한 캠퍼스 정보 앱")
        String description,

        @NotBlank(message = "카테고리를 입력해주세요.")
        @Schema(description = "카테고리", example = "모바일")
        String category,

        @Schema(description = "사용 기술 스택", example = "[\"React Native\", \"Spring Boot\", \"MySQL\"]")
        List<String> skills,

        @Min(value = 2, message = "최대 인원은 2명 이상이어야 합니다.")
        @Schema(description = "최대 팀원 수 (2명 이상)", example = "4")
        int maxMembers,

        @Future(message = "마감일은 미래 날짜여야 합니다.")
        @Schema(description = "모집 마감일 (미래 날짜, yyyy-MM-dd)", example = "2026-06-30")
        LocalDate deadline
) {}
