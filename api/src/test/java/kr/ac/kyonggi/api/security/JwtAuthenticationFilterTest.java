package kr.ac.kyonggi.api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String VALID_SECRET = "test-secret-key-for-jwt-testing-32-characters-long";
    private static final long EXPIRATION_MS = 3_600_000L;

    private JwtTokenProvider jwtTokenProvider;
    private JwtAuthenticationFilter filter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(VALID_SECRET, EXPIRATION_MS);
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── Authorization 헤더 없음 ───────────────────────────────────────────────

    @Test
    @DisplayName("Authorization 헤더 없으면 SecurityContext에 Authentication 미설정")
    void doFilterInternal_noAuthorizationHeader_noAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // ── Bearer 접두사 없는 헤더 ── resolveToken() 미매칭 분기 ────────────────────

    @Test
    @DisplayName("Authorization 헤더가 'Bearer '로 시작하지 않으면 SecurityContext에 Authentication 미설정")
    void doFilterInternal_nonBearerHeader_noAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더가 'Bearer'만 있고 공백 없으면 SecurityContext에 Authentication 미설정")
    void doFilterInternal_bearerWithoutSpace_noAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // ── 유효하지 않은 토큰 ── validate() false 분기 ──────────────────────────────

    @Test
    @DisplayName("유효하지 않은 Bearer 토큰이면 SecurityContext에 Authentication 미설정")
    void doFilterInternal_invalidToken_noAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer this.is.invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("만료된 토큰이면 SecurityContext에 Authentication 미설정")
    void doFilterInternal_expiredToken_noAuthentication() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("user@test.com")
                .claim("role", "ROLE_USER")
                .issuedAt(new Date(0))
                .expiration(new Date(1))
                .signWith(key)
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expiredToken);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    // ── 유효한 토큰 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("유효한 Bearer 토큰이면 SecurityContext에 Authentication 설정")
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        String token = jwtTokenProvider.generate("user@test.com", "ROLE_USER");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("user@test.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효한 토큰으로 인증 후 Authentication의 authority가 올바름")
    void doFilterInternal_validToken_correctAuthority() throws Exception {
        String token = jwtTokenProvider.generate("user@test.com", "ROLE_USER");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_USER");
    }

    // ── filterChain.doFilter() 항상 호출 확인 ───────────────────────────────────

    @Test
    @DisplayName("어떤 경우에도 filterChain.doFilter()는 반드시 호출됨")
    void doFilterInternal_alwaysCallsFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
