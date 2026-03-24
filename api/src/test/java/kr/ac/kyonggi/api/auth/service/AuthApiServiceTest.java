package kr.ac.kyonggi.api.auth.service;

import kr.ac.kyonggi.api.dto.request.RegisterRequest;
import kr.ac.kyonggi.api.dto.response.UserResponse;
import kr.ac.kyonggi.api.service.AuthApiService;
import kr.ac.kyonggi.common.exception.UserAlreadyExistsException;
import kr.ac.kyonggi.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class AuthApiServiceTest {

    @Autowired
    private AuthApiService authApiService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("신규 이메일로 회원가입 성공")
    void register_newEmail_success() {
        RegisterRequest request = new RegisterRequest("test@test.com", "password123", "홍길동");

        UserResponse response = authApiService.register(request);

        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.name()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 시 UserAlreadyExistsException 발생")
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest("test@test.com", "password123", "홍길동");
        authApiService.register(request);

        assertThatThrownBy(() -> authApiService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    @DisplayName("회원가입 시 비밀번호는 단방향 암호화되어 저장됨")
    void register_passwordIsEncoded() {
        authApiService.register(new RegisterRequest("test@test.com", "password123", "홍길동"));

        String storedPassword = userRepository.findByEmail("test@test.com").orElseThrow().getPassword();

        assertThat(storedPassword).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", storedPassword)).isTrue();
    }

    @Test
    @DisplayName("이메일로 UserResponse 조회")
    void findByEmail_returnsUserResponse() {
        authApiService.register(new RegisterRequest("test@test.com", "password123", "홍길동"));

        UserResponse response = authApiService.findByEmail("test@test.com");

        assertThat(response.email()).isEqualTo("test@test.com");
        assertThat(response.name()).isEqualTo("홍길동");
    }
}
