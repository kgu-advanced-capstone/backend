package kr.ac.kyonggi.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
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
        HttpHeaders registerHeaders = new HttpHeaders();
        registerHeaders.setContentType(MediaType.APPLICATION_JSON);
        String registerBody = """
                {
                  "email": "flow@test.com",
                  "password": "password123",
                  "name": "흐름테스트"
                }
                """;
        ResponseEntity<Map> registerResponse = restTemplate.exchange(
                "/api/auth/register",
                HttpMethod.POST,
                new HttpEntity<>(registerBody, registerHeaders),
                Map.class);
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. 로그인
        String loginBody = """
                {
                  "email": "flow@test.com",
                  "password": "password123"
                }
                """;
        ResponseEntity<Map> loginResponse = restTemplate.exchange(
                "/api/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(loginBody, registerHeaders),
                Map.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).containsKey("email");

        // 3. Set-Cookie에서 JSESSIONID 추출
        List<String> cookies = loginResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertThat(cookies).isNotNull();
        String sessionCookie = cookies.stream()
                .filter(c -> c.startsWith("JSESSIONID"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("JSESSIONID 쿠키가 없습니다."));
        String jsessionId = sessionCookie.split(";")[0];

        // 4. 세션 쿠키로 /me 접근
        HttpHeaders meHeaders = new HttpHeaders();
        meHeaders.set(HttpHeaders.COOKIE, jsessionId);
        ResponseEntity<Map> meResponse = restTemplate.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                new HttpEntity<>(meHeaders),
                Map.class);
        assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(meResponse.getBody()).containsEntry("email", "flow@test.com");
        assertThat(meResponse.getBody()).containsEntry("name", "흐름테스트");
    }

    @Test
    @DisplayName("세션 없이 /me 접근 시 401 반환")
    void me_withoutSession_returns401() {
        ResponseEntity<Map> response = restTemplate.exchange(
                "/api/auth/me",
                HttpMethod.GET,
                HttpEntity.EMPTY,
                Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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

        // 첫 번째 가입
        restTemplate.exchange("/api/auth/register", HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class);

        // 두 번째 가입 (중복)
        ResponseEntity<Map> response = restTemplate.exchange("/api/auth/register", HttpMethod.POST,
                new HttpEntity<>(body, headers), Map.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
