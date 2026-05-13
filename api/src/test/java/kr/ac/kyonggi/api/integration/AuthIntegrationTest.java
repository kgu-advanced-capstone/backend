package kr.ac.kyonggi.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("회원가입 → 로그인 → /me 전체 흐름 테스트")
    void fullAuthFlow_register_login_me() {
        // 1. 회원가입
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        String registerBody = """
                {
                  "email": "flow@test.com",
                  "password": "password123",
                  "name": "흐름테스트"
                }
                """;
        ResponseEntity<Map> registerResponse = restTemplate.exchange(
                "/api/auth/register", HttpMethod.POST,
                new HttpEntity<>(registerBody, jsonHeaders), Map.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. 로그인 → accessToken 반환
        String loginBody = """
                {
                  "email": "flow@test.com",
                  "password": "password123"
                }
                """;
        ResponseEntity<Map> loginResponse = restTemplate.exchange(
                "/api/auth/login", HttpMethod.POST,
                new HttpEntity<>(loginBody, jsonHeaders), Map.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).containsKey("accessToken");
        String accessToken = (String) loginResponse.getBody().get("accessToken");

        // 3. Bearer 토큰으로 /me 접근
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);
        ResponseEntity<Map> meResponse = restTemplate.exchange(
                "/api/auth/me", HttpMethod.GET,
                new HttpEntity<>(authHeaders), Map.class);
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meResponse.getBody()).containsEntry("email", "flow@test.com");
        assertThat(meResponse.getBody()).containsEntry("name", "흐름테스트");
    }

    @Test
    @DisplayName("토큰 없이 /me 접근 시 401 반환")
    void me_withoutToken_returns401() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/auth/me", HttpMethod.GET,
                HttpEntity.EMPTY, Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("로그아웃 후 클라이언트가 토큰을 폐기하면 /me 401 반환")
    void logout_clientDiscardsToken_returns401() {
        // 1. 회원가입 + 로그인
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        String registerBody = """
                {
                  "email": "logout@test.com",
                  "password": "password123",
                  "name": "로그아웃테스트"
                }
                """;
        restTemplate.exchange("/api/auth/register", HttpMethod.POST,
                new HttpEntity<>(registerBody, jsonHeaders), Map.class);

        String loginBody = """
                {
                  "email": "logout@test.com",
                  "password": "password123"
                }
                """;
        ResponseEntity<Map> loginResponse = restTemplate.exchange(
                "/api/auth/login", HttpMethod.POST,
                new HttpEntity<>(loginBody, jsonHeaders), Map.class);
        String accessToken = (String) loginResponse.getBody().get("accessToken");

        // 2. 로그아웃 → 204
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setBearerAuth(accessToken);
        ResponseEntity<Void> logoutResponse = restTemplate.exchange(
                "/api/auth/logout", HttpMethod.POST,
                new HttpEntity<>(authHeaders), Void.class);
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 3. 토큰 폐기 후 빈 헤더로 /me 접근 → 401
        ResponseEntity<Map> meResponse = restTemplate.exchange(
                "/api/auth/me", HttpMethod.GET,
                HttpEntity.EMPTY, Map.class);
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("중복 이메일 회원가입 시 409 반환")
    void register_duplicateEmail_returns409() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {
                  "email": "dup@test.com",
                  "password": "password123",
                  "name": "중복테스트"
                }
                """;

        restTemplate.exchange("/api/auth/register", HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class);

        ResponseEntity<Map> response = restTemplate.exchange("/api/auth/register", HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
