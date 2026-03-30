package kr.ac.kyonggi.api.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.ac.kyonggi.api.auth.dto.LoginRequest;
import kr.ac.kyonggi.api.auth.dto.RegisterRequest;
import kr.ac.kyonggi.api.auth.dto.UserResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Auth", description = "인증 API — 회원가입, 로그인, 내 정보 조회, 로그아웃")
public interface AuthApi {

    @Operation(
            summary = "회원가입",
            description = "새 계정을 생성합니다. 성공 시 생성된 사용자 정보를 반환합니다.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 이메일", content = @Content)
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    UserResponse register(@Valid @RequestBody RegisterRequest request);

    @Operation(
            summary = "로그인",
            description = "이메일/비밀번호로 로그인합니다. 성공 시 JSESSIONID 쿠키가 설정됩니다. " +
                    "이 엔드포인트는 Spring Security 필터가 처리합니다.",
            security = {}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치", content = @Content)
    })
    @PostMapping("/login")
    default ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        throw new UnsupportedOperationException("Spring Security 필터가 처리합니다.");
    }

    @Operation(
            summary = "내 정보 조회",
            description = "현재 인증된 사용자의 정보를 반환합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @GetMapping("/me")
    ResponseEntity<UserResponse> me(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "로그아웃",
            description = "현재 세션을 무효화합니다.",
            security = {}
    )
    @ApiResponse(responseCode = "204", description = "로그아웃 성공", content = @Content)
    @PostMapping("/logout")
    ResponseEntity<Void> logout(HttpServletRequest request);
}
