package kr.ac.kyonggi.api.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.kyonggi.api.auth.AuthApiService;
import kr.ac.kyonggi.api.config.SecurityConfig;
import kr.ac.kyonggi.api.profile.dto.ProfileResponse;
import kr.ac.kyonggi.api.profile.dto.UpdateProfileRequest;
import kr.ac.kyonggi.api.security.CustomUserDetailsService;
import kr.ac.kyonggi.api.security.JwtAuthenticationFilter;
import kr.ac.kyonggi.api.security.JwtTokenProvider;
import kr.ac.kyonggi.api.security.LoginSuccessHandler;
import kr.ac.kyonggi.api.security.OAuth2LoginSuccessHandler;
import kr.ac.kyonggi.common.exception.CustomAuthenticationEntryPoint;
import kr.ac.kyonggi.infrastructure.oauth.CustomOAuth2UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, LoginSuccessHandler.class, JwtTokenProvider.class, JwtAuthenticationFilter.class, CustomAuthenticationEntryPoint.class})
@ActiveProfiles("test")
class ProfileControllerTest {

    @Autowired
    MockMvcTester mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ProfileApiService profileApiService;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    @MockitoBean
    CustomOAuth2UserService customOAuth2UserService;

    @MockitoBean
    OAuth2LoginSuccessHandler oauth2LoginSuccessHandler;

    @MockitoBean
    AuthApiService authApiService;

    private static final String EMAIL = "test@test.com";
    private static final ProfileResponse PROFILE_RESPONSE =
            new ProfileResponse("홍길동", EMAIL, "010-0000-0000", "https://github.com/test", null, null);

    @Test
    @WithAnonymousUser
    @DisplayName("GET /api/profile - 미인증 요청 401 반환")
    void getProfile_unauthenticated_returns401() {
        assertThat(mockMvc.get().uri("/api/profile"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("GET /api/profile - 인증된 사용자 200 및 프로필 응답")
    void getProfile_authenticated_returns200() {
        when(profileApiService.getProfile(EMAIL)).thenReturn(PROFILE_RESPONSE);

        assertThat(mockMvc.get().uri("/api/profile"))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.email").asString().isEqualTo(EMAIL);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("PATCH /api/profile - 미인증 요청 401 반환")
    void updateProfile_unauthenticated_returns401() throws JsonProcessingException {
        MockMultipartFile request = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                toJson(new UpdateProfileRequest("홍길동", null, null, null)).getBytes());

        assertThat(mockMvc.patch().uri("/api/profile")
                .multipart()
                .file(request))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("PATCH /api/profile - 유효한 요청 200 및 업데이트된 프로필 반환")
    void updateProfile_validRequest_returns200() throws JsonProcessingException {
        ProfileResponse updated = new ProfileResponse("새이름", EMAIL, null, null, null, null);
        when(profileApiService.updateProfile(eq(EMAIL), any(), any())).thenReturn(updated);

        MockMultipartFile request = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                toJson(new UpdateProfileRequest("새이름", null, null, null)).getBytes());

        assertThat(mockMvc.patch().uri("/api/profile")
                .multipart()
                .file(request))
                .hasStatus(HttpStatus.OK)
                .bodyJson()
                .extractingPath("$.name").asString().isEqualTo("새이름");
    }

    @Test
    @WithMockUser(username = EMAIL)
    @DisplayName("PATCH /api/profile - 이름 1자 이하 400 반환")
    void updateProfile_nameTooShort_returns400() throws JsonProcessingException {
        MockMultipartFile request = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                toJson(new UpdateProfileRequest("X", null, null, null)).getBytes());

        assertThat(mockMvc.patch().uri("/api/profile")
                .multipart()
                .file(request))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    private String toJson(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
