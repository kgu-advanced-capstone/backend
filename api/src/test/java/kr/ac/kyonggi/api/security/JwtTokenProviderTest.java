package kr.ac.kyonggi.api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;



class JwtTokenProviderTest {

    private static final String VALID_SECRET = "test-secret-key-for-jwt-testing-32-characters-long";
    private static final long EXPIRATION_MS = 3_600_000L;

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(VALID_SECRET, EXPIRATION_MS);
    }

    // ── 생성자 ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("생성자: 32바이트 이상 시크릿이면 정상 생성")
    void constructor_validSecret_createsInstance() {
        JwtTokenProvider p = new JwtTokenProvider(VALID_SECRET, EXPIRATION_MS);
        assertThat(p).isNotNull();
    }

    @Test
    @DisplayName("생성자: 32바이트 미만 시크릿이면 IllegalArgumentException 발생")
    void constructor_shortSecret_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new JwtTokenProvider("short-key", EXPIRATION_MS))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32바이트");
    }

    // ── generate() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("generate: 토큰을 반환하며 null이 아님")
    void generate_returnsNonNullToken() {
        String token = provider.generate("user@test.com", "ROLE_USER");
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("generate: 생성된 토큰은 점(.) 두 개로 구분된 JWT 형식")
    void generate_tokenHasJwtStructure() {
        String token = provider.generate("user@test.com", "ROLE_USER");
        assertThat(token.split("\\.")).hasSize(3);
    }

    // ── validate() ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("validate: 유효한 토큰이면 true 반환")
    void validate_validToken_returnsTrue() {
        String token = provider.generate("user@test.com", "ROLE_USER");
        assertThat(provider.validate(token)).isTrue();
    }

    @Test
    @DisplayName("validate: 만료된 토큰이면 false 반환 — JwtException catch 분기")
    void validate_expiredToken_returnsFalse() {
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("user@test.com")
                .claim("role", "ROLE_USER")
                .issuedAt(new Date(0))
                .expiration(new Date(1))
                .signWith(key)
                .compact();
        assertThat(provider.validate(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("validate: subject 없는 토큰이면 false 반환")
    void validate_tokenMissingSubject_returnsFalse() {
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
        String noSubjectToken = Jwts.builder()
                .claim("role", "ROLE_USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
        assertThat(provider.validate(noSubjectToken)).isFalse();
    }

    @Test
    @DisplayName("validate: role 클레임 없는 토큰이면 false 반환")
    void validate_tokenMissingRole_returnsFalse() {
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));
        String noRoleToken = Jwts.builder()
                .subject("user@test.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key)
                .compact();
        assertThat(provider.validate(noRoleToken)).isFalse();
    }

    @Test
    @DisplayName("validate: 다른 키로 서명된 토큰이면 false 반환 — 서명 위변조 분기")
    void validate_tamperedSignature_returnsFalse() {
        String otherSecret = "another-secret-key-for-jwt-testing-32bytes!!";
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherSecret, EXPIRATION_MS);
        String tokenFromOther = otherProvider.generate("user@test.com", "ROLE_USER");

        // 다른 키로 서명된 토큰을 현재 provider로 검증하면 false
        assertThat(provider.validate(tokenFromOther)).isFalse();
    }

    @Test
    @DisplayName("validate: 완전히 잘못된 형식의 문자열이면 false 반환 — IllegalArgumentException catch 분기")
    void validate_malformedToken_returnsFalse() {
        assertThat(provider.validate("this.is.not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("validate: 빈 문자열이면 false 반환")
    void validate_emptyString_returnsFalse() {
        assertThat(provider.validate("")).isFalse();
    }

    // ── getAuthentication() ──────────────────────────────────────────────────

    @Test
    @DisplayName("getAuthentication: Authentication의 getName()이 토큰 subject(email)와 일치")
    void getAuthentication_nameEqualsEmail() {
        String email = "user@test.com";
        String token = provider.generate(email, "ROLE_USER");

        Authentication auth = provider.getAuthentication(token);

        assertThat(auth.getName()).isEqualTo(email);
    }

    @Test
    @DisplayName("getAuthentication: Authentication에 토큰에 담긴 role authority가 포함")
    void getAuthentication_authorityEqualsRole() {
        String role = "ROLE_USER";
        String token = provider.generate("user@test.com", role);

        Authentication auth = provider.getAuthentication(token);

        assertThat(auth.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly(role);
    }

    @Test
    @DisplayName("getAuthentication: 반환된 Authentication은 인증된 상태(credentials 없이도 isAuthenticated true)")
    void getAuthentication_isAuthenticated() {
        String token = provider.generate("user@test.com", "ROLE_USER");

        Authentication auth = provider.getAuthentication(token);

        assertThat(auth.isAuthenticated()).isTrue();
    }

    // ── generate + validate 왕복 검증 ─────────────────────────────────────────

    @Test
    @DisplayName("generate → validate → getAuthentication 전체 흐름 정상 동작")
    void fullFlow_generateValidateGetAuthentication() {
        String email = "full@test.com";
        String role = "ROLE_ADMIN";

        String token = provider.generate(email, role);
        assertThat(provider.validate(token)).isTrue();

        Authentication auth = provider.getAuthentication(token);
        assertThat(auth.getName()).isEqualTo(email);
        assertThat(auth.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactly(role);
    }
}
