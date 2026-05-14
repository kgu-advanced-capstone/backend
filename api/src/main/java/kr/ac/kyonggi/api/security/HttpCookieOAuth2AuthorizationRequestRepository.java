package kr.ac.kyonggi.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    private final ObjectMapper objectMapper;

    public HttpCookieOAuth2AuthorizationRequestRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper.copy();
        this.objectMapper.registerModules(SecurityJackson2Modules.getModules(getClass().getClassLoader()));
        this.objectMapper.registerModule(new OAuth2ClientJackson2Module());
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookieValue(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(request, response);
            return;
        }
        String encoded;
        try {
            encoded = Base64.getUrlEncoder().encodeToString(
                    objectMapper.writeValueAsBytes(authorizationRequest));
        } catch (Exception e) {
            return;
        }
        ResponseCookie cookie = ResponseCookie.from(COOKIE_NAME, encoded)
                .path("/")
                .httpOnly(true)
                .maxAge(COOKIE_EXPIRE_SECONDS)
                .secure(true)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
            HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        deleteCookie(request, response);
        return authRequest;
    }

    private OAuth2AuthorizationRequest getCookieValue(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                try {
                    byte[] decoded = Base64.getUrlDecoder().decode(cookie.getValue());
                    return objectMapper.readValue(decoded, OAuth2AuthorizationRequest.class);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() == null) return;
        for (Cookie cookie : request.getCookies()) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                ResponseCookie expired = ResponseCookie.from(COOKIE_NAME, "")
                        .path("/")
                        .httpOnly(true)
                        .maxAge(0)
                        .secure(true)
                        .sameSite("Lax")
                        .build();
                response.addHeader(HttpHeaders.SET_COOKIE, expired.toString());
                break;
            }
        }
    }
}
