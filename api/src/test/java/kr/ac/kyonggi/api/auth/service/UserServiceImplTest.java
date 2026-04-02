package kr.ac.kyonggi.api.auth.service;

import kr.ac.kyonggi.api.config.JpaTestConfig;
import kr.ac.kyonggi.common.exception.UserNotFoundException;
import kr.ac.kyonggi.domain.user.UpdateProfileCommand;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    @DisplayName("updateProfile()은 null이 아닌 필드만 수정된다")
    void updateProfile_updatesNonNullFields() {
        User saved = userService.register(buildUser("test@test.com"));

        User updated = userService.updateProfile(saved.getId(),
                new UpdateProfileCommand("새이름", "010-1234-5678", "https://github.com/test", "https://blog.test", null));

        assertThat(updated.getName()).isEqualTo("새이름");
        assertThat(updated.getPhone()).isEqualTo("010-1234-5678");
        assertThat(updated.getGithub()).isEqualTo("https://github.com/test");
        assertThat(updated.getBlog()).isEqualTo("https://blog.test");
        assertThat(updated.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("updateProfile()은 null 필드를 기존 값으로 유지한다")
    void updateProfile_nullFields_notOverwritten() {
        User saved = userService.register(buildUser("test@test.com"));
        userService.updateProfile(saved.getId(),
                new UpdateProfileCommand(null, "010-1111-2222", null, null, null));

        User result = userService.updateProfile(saved.getId(),
                new UpdateProfileCommand(null, null, "https://github.com/test", null, null));

        assertThat(result.getPhone()).isEqualTo("010-1111-2222");
        assertThat(result.getGithub()).isEqualTo("https://github.com/test");
    }

    @Test
    @DisplayName("updateProfile()은 존재하지 않는 ID면 UserNotFoundException을 던진다")
    void updateProfile_nonExistingId_throwsUserNotFoundException() {
        assertThatThrownBy(() -> userService.updateProfile(99L,
                new UpdateProfileCommand("이름", null, null, null, null)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("getAllByIds()는 ID 목록에 해당하는 User 목록을 반환한다")
    void getAllByIds_returnsUsers() {
        User user1 = userService.register(buildUser("a@test.com"));
        User user2 = userService.register(buildUser("b@test.com"));

        List<User> result = userService.getAllByIds(List.of(user1.getId(), user2.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getEmail)
                .containsExactlyInAnyOrder("a@test.com", "b@test.com");
    }
}
