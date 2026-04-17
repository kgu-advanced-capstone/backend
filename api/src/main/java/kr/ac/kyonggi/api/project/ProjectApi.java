package kr.ac.kyonggi.api.project;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.kyonggi.api.project.dto.CreateProjectRequest;
import kr.ac.kyonggi.api.project.dto.MyProjectResponse;
import kr.ac.kyonggi.api.project.dto.ProjectDetailResponse;
import kr.ac.kyonggi.api.project.dto.ProjectListResponse;
import kr.ac.kyonggi.api.project.dto.UpdateProjectStatusRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Tag(name = "Project", description = "프로젝트 API — 조회, 생성, 지원, 상태 변경")
public interface ProjectApi {

    @Operation(
            summary = "프로젝트 목록 조회",
            description = "모든 프로젝트를 필터링 및 페이지네이션하여 조회합니다. 인증 불필요.",
            security = {}
    )
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ProjectListResponse.class)))
    @GetMapping
    ProjectListResponse getProjects(
            @Parameter(description = "카테고리 필터 (선택)", example = "모바일")
            @RequestParam(required = false) String category,
            @Parameter(description = "페이지 번호 (기본값: 1)", example = "1",
                    schema = @Schema(type = "integer", defaultValue = "1", minimum = "1"))
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수 (기본값: 10)", example = "10",
                    schema = @Schema(type = "integer", defaultValue = "10", minimum = "1"))
            @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "검색어 (선택)", example = "캠퍼스")
            @RequestParam(required = false) String search
    );

    @Operation(
            summary = "내 프로젝트 목록 조회",
            description = "참여 중이거나 생성한 프로젝트 목록을 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = MyProjectResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @GetMapping("/my")
    List<MyProjectResponse> getMyProjects(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "특정 프로젝트의 상세 정보를 조회합니다. 인증 불필요.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ProjectDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "프로젝트 없음", content = @Content)
    })
    @GetMapping("/{id}")
    ProjectDetailResponse getProject(
            @Parameter(description = "프로젝트 ID", example = "1", required = true)
            @PathVariable Long id
    );

    @Operation(
            summary = "프로젝트 생성",
            description = "새 프로젝트를 생성합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "프로젝트 생성 성공",
                    content = @Content(schema = @Schema(implementation = ProjectDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ProjectDetailResponse createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "프로젝트 지원",
            description = "특정 프로젝트에 팀원으로 지원합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지원 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 지원한 프로젝트", content = @Content)
    })
    @PostMapping("/{id}/apply")
    ResponseEntity<Void> applyProject(
            @Parameter(description = "프로젝트 ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "프로젝트 상태 변경",
            description = "프로젝트 상태를 변경합니다. 프로젝트 생성자만 가능합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            examples = {
                                    @ExampleObject(name = "모집 중", value = "{\"status\": \"recruiting\"}"),
                                    @ExampleObject(name = "진행 중", value = "{\"status\": \"in-progress\"}"),
                                    @ExampleObject(name = "완료", value = "{\"status\": \"completed\"}")
                            }
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공", content = @Content),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content),
            @ApiResponse(responseCode = "403", description = "권한 없음 (생성자 아님)", content = @Content)
    })
    @PatchMapping("/{id}/status")
    ResponseEntity<Void> updateStatus(
            @Parameter(description = "프로젝트 ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectStatusRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );
}
