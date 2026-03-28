package kr.ac.kyonggi.api.project.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.List;

public record CreateProjectRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        String title,

        String description,

        @NotBlank(message = "카테고리를 입력해주세요.")
        String category,

        List<String> skills,

        @Min(value = 2, message = "최대 인원은 2명 이상이어야 합니다.")
        int maxMembers,

        @Future(message = "마감일은 미래 날짜여야 합니다.")
        LocalDate deadline
) {}
