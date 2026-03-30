package kr.ac.kyonggi.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    @Test
    @DisplayName("create() 직후 isRead는 false이다")
    void create_initialIsReadIsFalse() {
        Notification notification = Notification.create(new NotificationCreateCommand(1L, "테스트 알림"));

        assertThat(notification.isRead()).isFalse();
    }

    @Test
    @DisplayName("create() 직후 message와 userId가 올바르게 설정된다")
    void create_setsMessageAndUserId() {
        Notification notification = Notification.create(new NotificationCreateCommand(1L, "테스트 알림"));

        assertThat(notification.getMessage()).isEqualTo("테스트 알림");
        assertThat(notification.getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("markAsRead()를 호출하면 isRead가 true로 변경된다")
    void markAsRead_setsIsReadToTrue() {
        Notification notification = Notification.create(new NotificationCreateCommand(1L, "테스트 알림"));

        notification.markAsRead();

        assertThat(notification.isRead()).isTrue();
    }
}
