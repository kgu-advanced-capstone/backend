package kr.ac.kyonggi.api.education.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.education.Education;

import java.time.LocalDate;

public record EducationResponse(

        @Schema(description = "학력 고유 ID", example = "1")
        Long id,

        @Schema(description = "학교명", example = "경기대학교")
        String schoolName,

        @Schema(description = "전공", example = "컴퓨터공학과")
        String major,

        @Schema(description = "학위", example = "학사")
        String degree,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "입학일 (yyyy-MM-dd)", example = "2020-03-01", type = "string")
        LocalDate startDate,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "졸업일 (yyyy-MM-dd, 재학 중이면 null)", example = "2024-02-29", type = "string")
        LocalDate endDate

) {
    public static EducationResponse from(Education education) {
        return new EducationResponse(
                education.getId(),
                education.getSchoolName(),
                education.getMajor(),
                education.getDegree(),
                education.getStartDate(),
                education.getEndDate()
        );
    }
}
