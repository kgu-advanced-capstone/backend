package kr.ac.kyonggi.api.experience;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.kyonggi.api.experience.dto.AiSummaryResponse;
import kr.ac.kyonggi.api.experience.dto.ExperienceRequest;
import kr.ac.kyonggi.api.experience.dto.ExperienceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Experience", description = "프로젝트 활동 기록 API")
public interface ExperienceApi {

    @Operation(
            summary = "프로젝트 활동 목록 조회",
            description = "특정 프로젝트에 대한 내 활동 기록 목록을 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = ExperienceResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content)
    })
    @GetMapping("/project/{projectId}")
    List<ExperienceResponse> getByProject(
            @Parameter(description = "프로젝트 ID", example = "1", required = true)
            @PathVariable Long projectId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "활동 기록 등록/수정",
            description = "프로젝트 활동 기록을 등록하거나 기존 기록을 수정합니다 (upsert).",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록/수정 성공",
                    content = @Content(schema = @Schema(implementation = ExperienceResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @PostMapping("/project/{projectId}")
    ResponseEntity<ExperienceResponse> upsert(
            @Parameter(description = "프로젝트 ID", example = "1", required = true)
            @PathVariable Long projectId,
            @Valid @RequestBody ExperienceRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "활동 기록 AI 요약",
            description = "활동 기록을 AI가 분석하여 요약합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "요약 성공",
                    content = @Content(schema = @Schema(implementation = AiSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "404", description = "활동 기록 없음", content = @Content)
    })
    @PostMapping("/{id}/summarize")
    ResponseEntity<AiSummaryResponse> summarize(
            @Parameter(description = "활동 기록 ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );
}
