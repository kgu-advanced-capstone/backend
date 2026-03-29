package kr.ac.kyonggi.domain.notification;

import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(new UserCreateCommand("test@test.com", "pw", "홍길동", null));
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    @DisplayName("create() 직후 isRead는 false이다")
    void create_initialIsReadIsFalse() {
        Notification notification = Notification.create(new NotificationCreateCommand(user, "테스트 알림"));

        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("create() 직후 message와 user가 올바르게 설정된다")
    void create_setsMessageAndUser() {
        Notification notification = Notification.create(new NotificationCreateCommand(user, "테스트 알림"));

        assertThat(notification.getMessage()).isEqualTo("테스트 알림");
        assertThat(notification.getUser()).isSameAs(user);
    }

    @Test
    @DisplayName("markAsRead()를 호출하면 isRead가 true로 변경된다")
    void markAsRead_setsIsReadToTrue() {
        Notification notification = Notification.create(new NotificationCreateCommand(user, "테스트 알림"));

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }
}
