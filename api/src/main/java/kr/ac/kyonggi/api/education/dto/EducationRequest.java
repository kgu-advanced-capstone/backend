package kr.ac.kyonggi.api.education.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EducationRequest(

        @NotBlank(message = "학교명을 입력해주세요.")
        @Schema(description = "학교명", example = "경기대학교")
        String schoolName,

        @Schema(description = "전공", example = "컴퓨터공학과")
        String major,

        @Schema(description = "학위 (예: 고졸, 학사, 석사, 박사)", example = "학사")
        String degree,

        @NotNull(message = "입학일을 입력해주세요.")
        @Schema(description = "입학일 (yyyy-MM-dd)", example = "2020-03-01", type = "string")
        LocalDate startDate,

        @Schema(description = "졸업일 (yyyy-MM-dd, 재학 중이면 null)", example = "2024-02-29", type = "string")
        LocalDate endDate

) {}
