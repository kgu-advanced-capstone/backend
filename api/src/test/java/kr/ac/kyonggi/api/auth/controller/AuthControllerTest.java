package kr.ac.kyonggi.api.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.kyonggi.api.config.SecurityConfig;
import kr.ac.kyonggi.api.auth.AuthController;
import kr.ac.kyonggi.api.auth.dto.RegisterRequest;
import kr.ac.kyonggi.api.auth.dto.UserResponse;
import kr.ac.kyonggi.api.security.CustomUserDetailsService;
import kr.ac.kyonggi.api.security.LoginSuccessHandler;
import kr.ac.kyonggi.api.auth.AuthApiService;
import kr.ac.kyonggi.common.exception.UserAlreadyExistsException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, LoginSuccessHandler.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthApiService authApiService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    private static final String EMAIL = "test@test.com";
    private static final String PASSWORD = "password123";
    private static final String NAME = "홍길동";
    private static final UserResponse USER_RESPONSE = new UserResponse(1L, EMAIL, NAME, null);

    @Test
    @DisplayName("회원가입 성공 - 201 및 응답 본문 확인")
    void register_success() throws JsonProcessingException {
        when(authApiService.register(any())).thenReturn(USER_RESPONSE);

        assertThat(mockMvc.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new RegisterRequest(EMAIL, PASSWORD, NAME))))
                .hasStatus(HttpStatus.CREATED)
                .bodyJson()
                .extractingPath("$.email").asString().isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("회원가입 - 중복 이메일 409 반환")
    void register_duplicateEmail() throws JsonProcessingException {
        when(authApiService.register(any()))
                .thenThrow(new UserAlreadyExistsException("이미 사용 중인 이메일입니다."));

        assertThat(mockMvc.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new RegisterRequest(EMAIL, PASSWORD, NAME))))
                .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("회원가입 - 잘못된 이메일 형식 400 반환")
    void register_invalidEmail() throws JsonProcessingException {
        assertThat(mockMvc.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new RegisterRequest("not-an-email", PASSWORD, NAME))))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("회원가입 - 비밀번호 8자 미만 400 반환")
    void register_shortPassword() throws JsonProcessingException {
        assertThat(mockMvc.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new RegisterRequest(EMAIL, "short", NAME))))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("/me - 미인증 요청 401 반환")
    void me_unauthenticated() {
        assertThat(mockMvc.get().uri("/api/auth/me"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("/me - 인증된 사용자 200 및 응답 본문 확인")
    void me_authenticated() {
        when(authApiService.findByEmail(any())).thenReturn(USER_RESPONSE);

        assertThat(mockMvc.get().uri("/api/auth/me"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.email").asString().isEqualTo(EMAIL);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("로그아웃 - 미인증 사용자도 204 반환")
    void logout_unauthenticated() {
        assertThat(mockMvc.post().uri("/api/auth/logout"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    @WithMockUser(username = "test@test.com")
    @DisplayName("로그아웃 - 인증된 사용자 204 반환")
    void logout_authenticated() {
        assertThat(mockMvc.post().uri("/api/auth/logout"))
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
