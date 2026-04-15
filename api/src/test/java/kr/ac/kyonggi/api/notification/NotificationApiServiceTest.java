package kr.ac.kyonggi.api.notification;

import kr.ac.kyonggi.api.notification.dto.NotificationResponse;
import kr.ac.kyonggi.domain.notification.Notification;
import kr.ac.kyonggi.domain.notification.NotificationCreateCommand;
import kr.ac.kyonggi.domain.notification.NotificationService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import kr.ac.kyonggi.domain.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationApiServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationApiService notificationApiService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = User.create(new UserCreateCommand("test@test.com", "pw", "홍길동", null, null));
        ReflectionTestUtils.setField(user, "id", 1L);

        notification = Notification.create(new NotificationCreateCommand(1L, "\"테스트\" 프로젝트에 참가했습니다."));
        ReflectionTestUtils.setField(notification, "id", 10L);
        ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.of(2026, 3, 29, 14, 30));
    }

    // ── getNotifications() ────────────────────────────────────────────

    @Test
    @DisplayName("getNotifications()는 알림 목록을 NotificationResponse 리스트로 변환하여 반환한다")
    void getNotifications_returnsNotificationResponseList() {
        given(userService.getByEmail("test@test.com")).willReturn(user);
        given(notificationService.getByUserId(1L)).willReturn(List.of(notification));

        List<NotificationResponse> result = notificationApiService.getNotifications("test@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(10L);
        assertThat(result.get(0).message()).isEqualTo("\"테스트\" 프로젝트에 참가했습니다.");
        assertThat(result.get(0).time()).isEqualTo("오후 02:30");
        assertThat(result.get(0).read()).isFalse();
    }

    @Test
    @DisplayName("getNotifications()는 알림이 없으면 빈 리스트를 반환한다")
    void getNotifications_noNotifications_returnsEmptyList() {
        given(userService.getByEmail("test@test.com")).willReturn(user);
        given(notificationService.getByUserId(1L)).willReturn(List.of());

        List<NotificationResponse> result = notificationApiService.getNotifications("test@test.com");

        assertThat(result).isEmpty();
    }

    // ── markAsRead() ──────────────────────────────────────────────────

    @Test
    @DisplayName("markAsRead()는 UserService로 사용자를 조회하고 NotificationService.markAsRead()를 위임한다")
    void markAsRead_resolvesUserAndDelegatesToService() {
        given(userService.getByEmail("test@test.com")).willReturn(user);

        notificationApiService.markAsRead(10L, "test@test.com");

        verify(notificationService).markAsRead(10L, 1L);
    }

    // ── markAllAsRead() ───────────────────────────────────────────────

    @Test
    @DisplayName("markAllAsRead()는 UserService로 사용자를 조회하고 NotificationService.markAllAsRead()를 위임한다")
    void markAllAsRead_resolvesUserAndDelegatesToService() {
        given(userService.getByEmail("test@test.com")).willReturn(user);

        notificationApiService.markAllAsRead("test@test.com");

        verify(notificationService).markAllAsRead(1L);
    }
}
