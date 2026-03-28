package kr.ac.kyonggi.api.auth.service;

import kr.ac.kyonggi.api.config.JpaTestConfig;
import kr.ac.kyonggi.common.exception.UserNotFoundException;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import kr.ac.kyonggi.domain.user.UserService;
import kr.ac.kyonggi.domain.user.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import({UserServiceImpl.class, JpaTestConfig.class})
@ActiveProfiles("test")
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    private User buildUser(String email) {
        return User.create(new UserCreateCommand(email, "encoded", "테스트", null));
    }

    @Test
    @DisplayName("User 저장 후 ID 발급 확인")
    void register_success() {
        User saved = userService.register(buildUser("test@test.com"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("이메일로 저장된 User 조회 성공")
    void getByEmail_success() {
        userService.register(buildUser("test@test.com"));

        User found = userService.getByEmail("test@test.com");

        assertThat(found.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 조회 시 UserNotFoundException 발생")
    void getByEmail_notFound_throwsException() {
        assertThatThrownBy(() -> userService.getByEmail("unknown@test.com"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("ID로 저장된 User 조회 성공")
    void getById_success() {
        User saved = userService.register(buildUser("test@test.com"));

        User found = userService.getById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 UserNotFoundException 발생")
    void getById_notFound_throwsException() {
        assertThatThrownBy(() -> userService.getById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("이미 사용 중인 이메일은 isEmailTaken이 true 반환")
    void isEmailTaken_returnsTrue() {
        userService.register(buildUser("test@test.com"));

        assertThat(userService.isEmailTaken("test@test.com")).isTrue();
    }

    @Test
    @DisplayName("사용하지 않는 이메일은 isEmailTaken이 false 반환")
    void isEmailTaken_returnsFalse() {
        assertThat(userService.isEmailTaken("nobody@test.com")).isFalse();
    }
}
