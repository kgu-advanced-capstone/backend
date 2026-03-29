package kr.ac.kyonggi.api.resume;

import kr.ac.kyonggi.api.auth.AuthApiService;
import kr.ac.kyonggi.api.config.SecurityConfig;
import kr.ac.kyonggi.api.profile.dto.ProfileResponse;
import kr.ac.kyonggi.api.resume.dto.ResumeResponse;
import kr.ac.kyonggi.api.resume.dto.SummarizedExperienceResponse;
import kr.ac.kyonggi.api.security.CustomUserDetailsService;
import kr.ac.kyonggi.api.security.LoginSuccessHandler;
import kr.ac.kyonggi.common.exception.ResumeNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(ResumeController.class)
@Import({SecurityConfig.class, LoginSuccessHandler.class})
@ActiveProfiles("test")
class ResumeControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @MockitoBean
    ResumeApiService resumeApiService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    @MockitoBean
    AuthApiService authApiService;

    private static final String EMAIL = "test@test.com";

    private ResumeResponse sampleResumeResponse() {
        ProfileResponse profile = new ProfileResponse("홍길동", EMAIL, null, null, null, null);
        SummarizedExperienceResponse exp = new SummarizedExperienceResponse(
                1L, "AI 기반 헬스케어", List.of("JWT 인증 구현", "배포 자동화 구성"));
        return new ResumeResponse(profile, List.of(exp), LocalDateTime.now());
    }

    // ── GET /api/resume ─────────────────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/resume - 미인증 요청 401 반환")
    void getResume_unauthenticated_returns401() {
        assertThat(mockMvc.get().uri("/api/resume"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("GET /api/resume - 이력서 없으면 404 반환")
    void getResume_noResume_returns404() {
        when(resumeApiService.getResume(EMAIL))
                .thenThrow(new ResumeNotFoundException("이력서가 없습니다."));

        assertThat(mockMvc.get().uri("/api/resume"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("GET /api/resume - 이력서 있으면 200 및 summarizedExperiences 반환")
    void getResume_returnsResumeResponse() {
        when(resumeApiService.getResume(EMAIL)).thenReturn(sampleResumeResponse());

        assertThat(mockMvc.get().uri("/api/resume"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.summarizedExperiences[0].projectTitle")
                .asString().isEqualTo("AI 기반 헬스케어");
    }

    // ── POST /api/resume/generate ────────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/resume/generate - 미인증 요청 401 반환")
    void generate_unauthenticated_returns401() {
        assertThat(mockMvc.post().uri("/api/resume/generate"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("POST /api/resume/generate - 인증된 사용자 200 반환")
    void generate_authenticated_returns200() {
        assertThat(mockMvc.post().uri("/api/resume/generate"))
                .hasStatus(HttpStatus.OK);
    }
}
