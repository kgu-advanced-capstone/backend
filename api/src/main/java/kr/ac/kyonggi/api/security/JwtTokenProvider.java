package kr.ac.kyonggi.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs
    ) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("jwt.secret은 비어 있을 수 없습니다.");
        }
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("jwt.secret은 최소 32바이트(256비트) 이상이어야 합니다.");
        }
        if (expirationMs <= 0) {
            throw new IllegalArgumentException("jwt.expiration-ms는 0보다 커야 합니다.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // JWT 토큰 생성
    // email을 subject로, role을 claim으로 저장
    public String generate(String email, String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role은 비어 있을 수 없습니다.");
        }
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    // 토큰에서 Authentication 객체 추출
    // JwtAuthenticationFilter에서 SecurityContext에 저장할 때 사용
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        String role   = claims.get("role", String.class);

        List<SimpleGrantedAuthority> authorities = (role != null && !role.isBlank())
                ? List.of(new SimpleGrantedAuthority(role))
                : List.of();
        UserDetails principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
    }

    // 토큰 유효성 검증
    // 만료되거나 변조된 토큰이면 false 반환, subject/role 클레임 누락 시에도 false
    public boolean validate(String token) {
        try {
            Claims claims = parseClaims(token);
            String subject = claims.getSubject();
            String role    = claims.get("role", String.class);
            return subject != null && !subject.isBlank()
                    && role != null && !role.isBlank();
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}