package kr.ac.kyonggi.api.auth.service;

import kr.ac.kyonggi.common.exception.UserNotFoundException;
import kr.ac.kyonggi.domain.entity.User;
import kr.ac.kyonggi.domain.service.UserService;
import kr.ac.kyonggi.domain.service.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(UserServiceImpl.class)
@ActiveProfiles("test")
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    private User buildUser(String email) {
        return User.builder().email(email).password("encoded").name("테스트").build();
    }

    @Test
    @DisplayName("User 저장 후 ID 발급 확인")
    void save_success() {
        User saved = userService.save(buildUser("test@test.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("이메일로 저장된 User 조회 성공")
    void findByEmail_success() {
        userService.save(buildUser("test@test.com"));

        User found = userService.findByEmail("test@test.com");

        assertThat(found.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 UserNotFoundException 발생")
    void findByEmail_notFound_throwsException() {
        assertThatThrownBy(() -> userService.findByEmail("unknown@test.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("ID로 저장된 User 조회 성공")
    void findById_success() {
        User saved = userService.save(buildUser("test@test.com"));

        User found = userService.findById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 UserNotFoundException 발생")
    void findById_notFound_throwsException() {
        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("저장된 이메일은 existsByEmail이 true 반환")
    void existsByEmail_returnsTrue() {
        userService.save(buildUser("test@test.com"));

        assertThat(userService.existsByEmail("test@test.com")).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 이메일은 existsByEmail이 false 반환")
    void existsByEmail_returnsFalse() {
        assertThat(userService.existsByEmail("nobody@test.com")).isFalse();
    }
}
