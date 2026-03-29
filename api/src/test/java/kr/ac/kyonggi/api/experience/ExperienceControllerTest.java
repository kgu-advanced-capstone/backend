package kr.ac.kyonggi.api.experience;

import kr.ac.kyonggi.api.auth.AuthApiService;
import kr.ac.kyonggi.api.config.SecurityConfig;
import kr.ac.kyonggi.api.experience.dto.AiSummaryResponse;
import kr.ac.kyonggi.api.experience.dto.ExperienceResponse;
import kr.ac.kyonggi.api.security.CustomUserDetailsService;
import kr.ac.kyonggi.api.security.LoginSuccessHandler;
import kr.ac.kyonggi.common.exception.ExperienceNotFoundException;
import kr.ac.kyonggi.common.exception.ForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(ExperienceController.class)
@Import({SecurityConfig.class, LoginSuccessHandler.class})
@ActiveProfiles("test")
class ExperienceControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @MockitoBean
    ExperienceApiService experienceApiService;

    @MockitoBean
    AuthApiService authApiService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    private static final String EMAIL = "test@test.com";
    private static final ExperienceResponse SAMPLE_RESPONSE =
            new ExperienceResponse(100L, "로그인 기능을 구현했습니다.", null, null);

    // ── GET /api/experiences/project/{projectId} ───────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/experiences/project/{projectId} - 미인증 요청 401 반환")
    void getByProject_unauthenticated_returns401() {
        assertThat(mockMvc.get().uri("/api/experiences/project/10"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("GET /api/experiences/project/{projectId} - 멤버인 경우 200 및 목록 반환")
    void getByProject_member_returns200WithList() {
        when(experienceApiService.getByProject(eq(10L), eq(EMAIL)))
                .thenReturn(List.of(SAMPLE_RESPONSE));

        assertThat(mockMvc.get().uri("/api/experiences/project/10"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$[0].id").asNumber().isEqualTo(100);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("GET /api/experiences/project/{projectId} - 멤버가 아니면 403 반환")
    void getByProject_nonMember_returns403() {
        when(experienceApiService.getByProject(eq(10L), eq(EMAIL)))
                .thenThrow(new ForbiddenException("해당 프로젝트의 멤버만 접근할 수 있습니다."));

        assertThat(mockMvc.get().uri("/api/experiences/project/10"))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    // ── POST /api/experiences/project/{projectId} ──────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/experiences/project/{projectId} - 미인증 요청 401 반환")
    void upsert_unauthenticated_returns401() {
        assertThat(mockMvc.post().uri("/api/experiences/project/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"경험 내용\"}"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("POST /api/experiences/project/{projectId} - 멤버인 경우 200 및 경험 반환")
    void upsert_member_returns200WithResponse() {
        when(experienceApiService.upsert(eq(10L), any(), eq(EMAIL)))
                .thenReturn(SAMPLE_RESPONSE);

        assertThat(mockMvc.post().uri("/api/experiences/project/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"로그인 기능을 구현했습니다.\"}"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.content").asString().isEqualTo("로그인 기능을 구현했습니다.");
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("POST /api/experiences/project/{projectId} - content가 없으면 400 반환")
    void upsert_missingContent_returns400() {
        assertThat(mockMvc.post().uri("/api/experiences/project/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"\"}"))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("POST /api/experiences/project/{projectId} - 멤버가 아니면 403 반환")
    void upsert_nonMember_returns403() {
        when(experienceApiService.upsert(eq(10L), any(), eq(EMAIL)))
                .thenThrow(new ForbiddenException("해당 프로젝트의 멤버만 접근할 수 있습니다."));

        assertThat(mockMvc.post().uri("/api/experiences/project/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"경험 내용\"}"))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    // ── POST /api/experiences/{id}/summarize ───────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/experiences/{id}/summarize - 미인증 요청 401 반환")
    void summarize_unauthenticated_returns401() {
        assertThat(mockMvc.post().uri("/api/experiences/100/summarize"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("POST /api/experiences/{id}/summarize - 본인 경험이면 200 및 AI 요약 반환")
    void summarize_owner_returns200WithAiSummary() {
        when(experienceApiService.summarize(eq(100L), eq(EMAIL)))
                .thenReturn(new AiSummaryResponse(100L, "JWT 기반 로그인 인증 시스템 구축"));

        assertThat(mockMvc.post().uri("/api/experiences/100/summarize"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.aiSummary").asString().isEqualTo("JWT 기반 로그인 인증 시스템 구축");
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("POST /api/experiences/{id}/summarize - 본인 경험이 아니면 403 반환")
    void summarize_nonOwner_returns403() {
        when(experienceApiService.summarize(eq(100L), eq(EMAIL)))
                .thenThrow(new ForbiddenException("본인의 경험 기록만 요약할 수 있습니다."));

        assertThat(mockMvc.post().uri("/api/experiences/100/summarize"))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("POST /api/experiences/{id}/summarize - 경험이 없으면 404 반환")
    void summarize_notFound_returns404() {
        when(experienceApiService.summarize(eq(999L), eq(EMAIL)))
                .thenThrow(new ExperienceNotFoundException("경험 기록을 찾을 수 없습니다: 999"));

        assertThat(mockMvc.post().uri("/api/experiences/999/summarize"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }
}
