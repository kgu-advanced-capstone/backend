package kr.ac.kyonggi.api.certification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.ac.kyonggi.api.auth.AuthApiService;
import kr.ac.kyonggi.api.certification.dto.CertificationRequest;
import kr.ac.kyonggi.api.certification.dto.CertificationResponse;
import kr.ac.kyonggi.api.config.SecurityConfig;
import kr.ac.kyonggi.api.security.CustomUserDetailsService;
import kr.ac.kyonggi.api.security.JwtAuthenticationFilter;
import kr.ac.kyonggi.api.security.JwtTokenProvider;
import kr.ac.kyonggi.api.security.LoginSuccessHandler;
import kr.ac.kyonggi.api.security.OAuth2LoginSuccessHandler;
import kr.ac.kyonggi.common.exception.CertificationNotFoundException;
import kr.ac.kyonggi.common.exception.CustomAuthenticationEntryPoint;
import kr.ac.kyonggi.infrastructure.oauth.CustomOAuth2UserService;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@WebMvcTest(CertificationController.class)
@Import({SecurityConfig.class, LoginSuccessHandler.class, JwtTokenProvider.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class})
@ActiveProfiles("test")
class CertificationControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @MockitoBean
    CertificationApiService certificationApiService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    @MockitoBean
    AuthApiService authApiService;

    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    private static final String EMAIL = "test@test.com";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private CertificationResponse sampleResponse() {
        return new CertificationResponse(1L, "정보처리기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15));
    }

    // ── GET /api/certifications ──────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/certifications - 미인증 요청 401 반환")
    void getAll_unauthenticated_returns401() {
        assertThat(mockMvc.get().uri("/api/certifications"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("GET /api/certifications - 인증된 사용자 200 반환")
    void getAll_authenticated_returns200() {
        when(certificationApiService.getAll(EMAIL)).thenReturn(List.of(sampleResponse()));

        assertThat(mockMvc.get().uri("/api/certifications"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$[0].name")
                .asString().isEqualTo("정보처리기사");
    }

    // ── POST /api/certifications ─────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/certifications - 미인증 요청 401 반환")
    void create_unauthenticated_returns401() throws Exception {
        assertThat(mockMvc.post().uri("/api/certifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new CertificationRequest(
                        "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 11, 15)))))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("POST /api/certifications - 인증된 사용자 201 반환")
    void create_authenticated_returns201() throws Exception {
        when(certificationApiService.create(eq(EMAIL), any())).thenReturn(sampleResponse());

        assertThat(mockMvc.post().uri("/api/certifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new CertificationRequest(
                        "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 11, 15)))))
                .hasStatus(HttpStatus.CREATED);
    }

    // ── PUT /api/certifications/{id} ─────────────────────────────────────────

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("PUT /api/certifications/{id} - 없는 자격증이면 404 반환")
    void update_notFound_returns404() throws Exception {
        doThrow(new CertificationNotFoundException("자격증 정보를 찾을 수 없습니다: 99"))
                .when(certificationApiService).update(eq(EMAIL), eq(99L), any());

        assertThat(mockMvc.put().uri("/api/certifications/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new CertificationRequest(
                        "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 11, 15)))))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("PUT /api/certifications/{id} - 성공 시 200 반환")
    void update_success_returns200() throws Exception {
        when(certificationApiService.update(eq(EMAIL), eq(1L), any())).thenReturn(sampleResponse());

        assertThat(mockMvc.put().uri("/api/certifications/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new CertificationRequest(
                        "정보처리기사", "한국산업인력공단", LocalDate.of(2023, 11, 15)))))
                .hasStatus(HttpStatus.OK);
    }

    // ── DELETE /api/certifications/{id} ─────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("DELETE /api/certifications/{id} - 미인증 요청 401 반환")
    void delete_unauthenticated_returns401() {
        assertThat(mockMvc.delete().uri("/api/certifications/1"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("DELETE /api/certifications/{id} - 성공 시 204 반환")
    void delete_success_returns204() {
        assertThat(mockMvc.delete().uri("/api/certifications/1"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }
}
