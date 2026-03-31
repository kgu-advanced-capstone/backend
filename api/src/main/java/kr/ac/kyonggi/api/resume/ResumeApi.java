package kr.ac.kyonggi.api.resume;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.kyonggi.api.resume.dto.ResumeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Resume", description = "이력서 API — 조회 및 생성")
public interface ResumeApi {

    @Operation(
            summary = "이력서 조회",
            description = "현재 사용자의 이력서를 조회합니다. AI 요약된 활동 기록이 포함됩니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ResumeResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "404", description = "생성된 이력서 없음", content = @Content)
    })
    @GetMapping
    ResponseEntity<ResumeResponse> getResume(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "이력서 생성",
            description = "AI가 활동 기록을 분석하여 이력서를 생성합니다. 기존 이력서가 있으면 덮어씁니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "생성 요청 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @PostMapping("/generate")
    ResponseEntity<Void> generate(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );
}
