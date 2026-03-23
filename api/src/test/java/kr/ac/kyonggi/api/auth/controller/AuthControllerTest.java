package kr.ac.kyonggi.api.auth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.kyonggi.api.dto.request.LoginRequest;
import kr.ac.kyonggi.api.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private static final String EMAIL = "test@test.com";
    private static final String PASSWORD = "password123";
    private static final String NAME = "홍길동";

    @Test
    @DisplayName("회원가입 성공 - 201 및 응답 본문 확인")
    void register_success() throws JsonProcessingException {
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
        register();

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
    @DisplayName("로그인 성공 - 200 및 사용자 정보 반환")
    void login_success() throws JsonProcessingException {
        register();

        assertThat(login())
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.email").asString().isEqualTo(EMAIL);
    }

    @Test
    @DisplayName("로그인 - 잘못된 비밀번호 401 반환")
    void login_badCredentials() throws JsonProcessingException {
        register();

        assertThat(mockMvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new LoginRequest(EMAIL, "wrongpassword"))))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }


    @Test
    @DisplayName("/me - 미인증 요청 401 반환")
    void me_unauthenticated() {
        assertThat(mockMvc.get().uri("/api/auth/me"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("로그인 후 세션으로 /me 접근 성공")
    void me_afterLogin() throws JsonProcessingException {
        register();

        MvcTestResult loginResult = login();
        MockHttpSession session = (MockHttpSession) loginResult.getMvcResult().getRequest().getSession(false);

        assertThat(mockMvc.get().uri("/api/auth/me")
                .session(session))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.email").asString().isEqualTo(EMAIL);
    }

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    private void register() throws JsonProcessingException {
        mockMvc.post()
                .uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new RegisterRequest(EMAIL, PASSWORD, NAME)))
                .exchange();
    }

    private MvcTestResult login() throws JsonProcessingException {
        return mockMvc.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new LoginRequest(EMAIL, PASSWORD)))
                .exchange();
    }
}
