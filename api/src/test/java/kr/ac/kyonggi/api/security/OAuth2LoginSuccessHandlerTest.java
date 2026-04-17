package kr.ac.kyonggi.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuth2LoginSuccessHandlerTest {

    private static final String VALID_SECRET = "test-secret-key-for-jwt-testing-32-characters-long";
    private static final long EXPIRATION_MS = 3_600_000L;
    private static final String REDIRECT_URI = "http://localhost:3000/oauth2/callback";

    private JwtTokenProvider jwtTokenProvider;
    private OAuth2LoginSuccessHandler handler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(VALID_SECRET, EXPIRATION_MS);
        handler = new OAuth2LoginSuccessHandler(jwtTokenProvider);
        ReflectionTestUtils.setField(handler, "redirectUri", REDIRECT_URI);
    }

    // ── onAuthenticationSuccess() ─────────────────────────────────────────────

    @Test
    @DisplayName("OAuth2 로그인 성공 시 redirect URI로 sendRedirect 호출")
    void onAuthenticationSuccess_redirectsToUri() throws Exception {
        OAuth2AuthenticationToken token = buildAuthToken("oauth@test.com", "ROLE_USER");

        handler.onAuthenticationSuccess(request, response, token);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(urlCaptor.capture());

        assertThat(urlCaptor.getValue()).startsWith(REDIRECT_URI);
    }

    @Test
    @DisplayName("redirect URL에 token 쿼리 파라미터가 포함됨")
    void onAuthenticationSuccess_redirectUrlContainsTokenParam() throws Exception {
        OAuth2AuthenticationToken token = buildAuthToken("oauth@test.com", "ROLE_USER");

        handler.onAuthenticationSuccess(request, response, token);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(urlCaptor.capture());

        assertThat(urlCaptor.getValue()).contains("token=");
    }

    @Test
    @DisplayName("redirect URL의 token은 유효한 JWT — validate()가 true 반환")
    void onAuthenticationSuccess_tokenInRedirectUrlIsValidJwt() throws Exception {
        OAuth2AuthenticationToken authToken = buildAuthToken("oauth@test.com", "ROLE_USER");

        handler.onAuthenticationSuccess(request, response, authToken);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(urlCaptor.capture());

        String jwt = extractToken(urlCaptor.getValue());
        assertThat(jwtTokenProvider.validate(jwt)).isTrue();
    }

    @Test
    @DisplayName("JWT subject가 OAuth2User의 email attribute와 일치")
    void onAuthenticationSuccess_jwtSubjectEqualsOAuth2Email() throws Exception {
        String email = "oauth@test.com";
        OAuth2AuthenticationToken authToken = buildAuthToken(email, "ROLE_USER");

        handler.onAuthenticationSuccess(request, response, authToken);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(urlCaptor.capture());

        String jwt = extractToken(urlCaptor.getValue());
        assertThat(jwtTokenProvider.getAuthentication(jwt).getName()).isEqualTo(email);
    }

    @Test
    @DisplayName("JWT authority가 OAuth2 인증의 role authority와 일치")
    void onAuthenticationSuccess_jwtAuthorityEqualsOAuth2Role() throws Exception {
        String role = "ROLE_USER";
        OAuth2AuthenticationToken authToken = buildAuthToken("oauth@test.com", role);

        handler.onAuthenticationSuccess(request, response, authToken);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(urlCaptor.capture());

        String jwt = extractToken(urlCaptor.getValue());
        assertThat(jwtTokenProvider.getAuthentication(jwt).getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly(role);
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private OAuth2AuthenticationToken buildAuthToken(String email, String role) {
        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(role)),
                Map.of("email", email, "name", "Test User", "sub", "12345"),
                "email"
        );
        return new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), "google");
    }

    private String extractToken(String redirectUrl) {
        int idx = redirectUrl.indexOf("token=");
        assertThat(idx).isGreaterThan(-1);
        return redirectUrl.substring(idx + "token=".length());
    }
}
