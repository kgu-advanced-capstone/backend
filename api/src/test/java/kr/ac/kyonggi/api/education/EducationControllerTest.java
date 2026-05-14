package kr.ac.kyonggi.api.education;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kr.ac.kyonggi.api.auth.AuthApiService;
import kr.ac.kyonggi.api.config.SecurityConfig;
import kr.ac.kyonggi.api.education.dto.EducationRequest;
import kr.ac.kyonggi.api.education.dto.EducationResponse;
import kr.ac.kyonggi.api.security.CustomUserDetailsService;
import kr.ac.kyonggi.api.security.LoginSuccessHandler;
import kr.ac.kyonggi.common.exception.EducationNotFoundException;
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

@WebMvcTest(EducationController.class)
@Import({SecurityConfig.class, LoginSuccessHandler.class})
@ActiveProfiles("test")
class EducationControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @MockitoBean
    EducationApiService educationApiService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    @MockitoBean
    AuthApiService authApiService;

    private static final String EMAIL = "test@test.com";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private EducationResponse sampleResponse() {
        return new EducationResponse(1L, "경기대학교", "컴퓨터공학과", "학사",
                LocalDate.of(2020, 3, 1), null);
    }

    // ── GET /api/educations ──────────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/educations - 미인증 요청 401 반환")
    void getAll_unauthenticated_returns401() {
        assertThat(mockMvc.get().uri("/api/educations"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("GET /api/educations - 인증된 사용자 200 반환")
    void getAll_authenticated_returns200() {
        when(educationApiService.getAll(EMAIL)).thenReturn(List.of(sampleResponse()));

        assertThat(mockMvc.get().uri("/api/educations"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$[0].schoolName")
                .asString().isEqualTo("경기대학교");
    }

    // ── POST /api/educations ─────────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("POST /api/educations - 미인증 요청 401 반환")
    void create_unauthenticated_returns401() throws Exception {
        assertThat(mockMvc.post().uri("/api/educations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new EducationRequest(
                        "경기대학교", "컴퓨터공학과", "학사", LocalDate.of(2020, 3, 1), null))))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("POST /api/educations - 인증된 사용자 201 반환")
    void create_authenticated_returns201() throws Exception {
        when(educationApiService.create(eq(EMAIL), any())).thenReturn(sampleResponse());

        assertThat(mockMvc.post().uri("/api/educations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new EducationRequest(
                        "경기대학교", "컴퓨터공학과", "학사", LocalDate.of(2020, 3, 1), null))))
                .hasStatus(HttpStatus.CREATED);
    }

    // ── PUT /api/educations/{id} ─────────────────────────────────────────────

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("PUT /api/educations/{id} - 없는 학력이면 404 반환")
    void update_notFound_returns404() throws Exception {
        doThrow(new EducationNotFoundException("학력 정보를 찾을 수 없습니다: 99"))
                .when(educationApiService).update(eq(EMAIL), eq(99L), any());

        assertThat(mockMvc.put().uri("/api/educations/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new EducationRequest(
                        "경기대학교", "컴퓨터공학과", "학사", LocalDate.of(2020, 3, 1), null))))
                .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("PUT /api/educations/{id} - 성공 시 200 반환")
    void update_success_returns200() throws Exception {
        when(educationApiService.update(eq(EMAIL), eq(1L), any())).thenReturn(sampleResponse());

        assertThat(mockMvc.put().uri("/api/educations/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new EducationRequest(
                        "경기대학교", "컴퓨터공학과", "학사", LocalDate.of(2020, 3, 1), null))))
                .hasStatus(HttpStatus.OK);
    }

    // ── DELETE /api/educations/{id} ──────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("DELETE /api/educations/{id} - 미인증 요청 401 반환")
    void delete_unauthenticated_returns401() {
        assertThat(mockMvc.delete().uri("/api/educations/1"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("DELETE /api/educations/{id} - 성공 시 204 반환")
    void delete_success_returns204() {
        assertThat(mockMvc.delete().uri("/api/educations/1"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }
}
