package kr.ac.kyonggi.api.notification;

import kr.ac.kyonggi.api.auth.AuthApiService;
import kr.ac.kyonggi.api.config.SecurityConfig;
import kr.ac.kyonggi.api.notification.dto.NotificationResponse;
import kr.ac.kyonggi.api.security.CustomUserDetailsService;
import kr.ac.kyonggi.api.security.JwtAuthenticationFilter;
import kr.ac.kyonggi.api.security.JwtTokenProvider;
import kr.ac.kyonggi.api.security.LoginSuccessHandler;
import kr.ac.kyonggi.api.security.OAuth2LoginSuccessHandler;
import kr.ac.kyonggi.common.exception.CustomAuthenticationEntryPoint;
import kr.ac.kyonggi.common.exception.NotificationNotFoundException;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, LoginSuccessHandler.class, JwtTokenProvider.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class})
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @MockitoBean
    NotificationApiService notificationApiService;

    @MockitoBean
    AuthApiService authApiService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    private static final NotificationResponse SAMPLE_RESPONSE =
            new NotificationResponse(1L, "\"AI 헬스케어\" 프로젝트에 참가했습니다.", "오후 02:30", false);

    // ── GET /api/notifications ────────────────────────────────────────

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("GET /api/notifications - 인증된 사용자 200 및 알림 목록 반환")
    void getNotifications_authenticated_returns200WithList() {
        when(notificationApiService.getNotifications("test@test.com"))
                .thenReturn(List.of(SAMPLE_RESPONSE));

        assertThat(mockMvc.get().uri("/api/notifications"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$[0].id").asNumber().isEqualTo(1);
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("GET /api/notifications - 알림이 없으면 빈 배열 반환")
    void getNotifications_noNotifications_returnsEmptyArray() {
        when(notificationApiService.getNotifications("test@test.com")).thenReturn(List.of());

        assertThat(mockMvc.get().uri("/api/notifications"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$")
                .asList()
                .isEmpty();
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/notifications - 미인증 요청 401 반환")
    void getNotifications_unauthenticated_returns401() {
        assertThat(mockMvc.get().uri("/api/notifications"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    // ── PATCH /api/notifications/{id}/read ───────────────────────────

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("PATCH /api/notifications/{id}/read - 인증된 사용자 200 반환")
    void markAsRead_authenticated_returns200() {
        assertThat(mockMvc.patch().uri("/api/notifications/1/read"))
                .hasStatus(HttpStatus.OK);
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("PATCH /api/notifications/{id}/read - 존재하지 않는 알림이면 404 반환")
    void markAsRead_notFound_returns404() {
        doThrow(new NotificationNotFoundException("알림을 찾을 수 없습니다: 99"))
                .when(notificationApiService).markAsRead(eq(99L), any());

        assertThat(mockMvc.patch().uri("/api/notifications/99/read"))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("PATCH /api/notifications/{id}/read - 미인증 요청 401 반환")
    void markAsRead_unauthenticated_returns401() {
        assertThat(mockMvc.patch().uri("/api/notifications/1/read"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    // ── PATCH /api/notifications/read-all ────────────────────────────

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("PATCH /api/notifications/read-all - 인증된 사용자 200 반환")
    void markAllAsRead_authenticated_returns200() {
        assertThat(mockMvc.patch().uri("/api/notifications/read-all"))
                .hasStatus(HttpStatus.OK);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("PATCH /api/notifications/read-all - 미인증 요청 401 반환")
    void markAllAsRead_unauthenticated_returns401() {
        assertThat(mockMvc.patch().uri("/api/notifications/read-all"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }
}
