package kr.ac.kyonggi.api.resume;

import kr.ac.kyonggi.api.auth.AuthApiService;
import kr.ac.kyonggi.api.config.SecurityConfig;
import kr.ac.kyonggi.api.profile.dto.ProfileResponse;
import kr.ac.kyonggi.api.resume.dto.ResumeDraftRequest;
import kr.ac.kyonggi.api.resume.dto.ResumeResponse;
import kr.ac.kyonggi.api.resume.dto.SummarizedExperienceResponse;
import kr.ac.kyonggi.api.security.CustomUserDetailsService;
import kr.ac.kyonggi.api.security.HttpCookieOAuth2AuthorizationRequestRepository;
import kr.ac.kyonggi.api.security.JwtAuthenticationFilter;
import kr.ac.kyonggi.api.security.JwtTokenProvider;
import kr.ac.kyonggi.api.security.LoginSuccessHandler;
import kr.ac.kyonggi.api.security.OAuth2LoginSuccessHandler;
import kr.ac.kyonggi.common.exception.CustomAuthenticationEntryPoint;
import kr.ac.kyonggi.common.exception.ResumeNotFoundException;
import kr.ac.kyonggi.infrastructure.oauth.CustomOAuth2UserService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(ResumeController.class)
@Import({SecurityConfig.class, LoginSuccessHandler.class, JwtTokenProvider.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class, HttpCookieOAuth2AuthorizationRequestRepository.class})
@ActiveProfiles("test")
class ResumeControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @MockitoBean
    ResumeApiService resumeApiService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @MockitoBean
    AuthApiService authApiService;

    private static final String EMAIL = "test@test.com";

    private ResumeResponse sampleResumeResponse() {
        ProfileResponse profile = new ProfileResponse("홍길동", EMAIL, null, null, null, null);
        SummarizedExperienceResponse exp = new SummarizedExperienceResponse(
                1L, "AI 기반 헬스케어", List.of("Spring Boot", "MySQL"), List.of("JWT 인증 구현", "배포 자동화 구성"));
        return new ResumeResponse(profile, "자기소개서", "지원 동기", List.of(exp), List.of(), List.of(), LocalDateTime.now());
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

    // ── PATCH /api/resume ───────────────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("PATCH /api/resume - 미인증 요청 401 반환")
    void saveDraft_unauthenticated_returns401() {
        assertThat(mockMvc.patch().uri("/api/resume")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"coverLetterTitle\":\"자기소개서\",\"coverLetterContent\":\"지원 동기\"}"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("PATCH /api/resume - 유효한 요청 200 및 저장된 초안 반환")
    void saveDraft_validRequest_returns200() {
        when(resumeApiService.saveDraft(eq(EMAIL), any())).thenReturn(sampleResumeResponse());

        assertThat(mockMvc.patch().uri("/api/resume")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"coverLetterTitle\":\"자기소개서\",\"coverLetterContent\":\"지원 동기\"}"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.coverLetterTitle").asString().isEqualTo("자기소개서");
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
