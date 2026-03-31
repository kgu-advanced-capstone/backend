package kr.ac.kyonggi.domain.notification;

import kr.ac.kyonggi.common.exception.NotificationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification notification;

    @BeforeEach
    void setUp() {
        notification = Notification.create(new NotificationCreateCommand(1L, "\"테스트\" 프로젝트에 참가했습니다."));
        ReflectionTestUtils.setField(notification, "id", 10L);
    }

    // ── create() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("create()는 repository.save()를 호출하여 알림을 저장한다")
    void create_savesNotification() {
        notificationService.create(notification);

        verify(notificationRepository).save(notification);
    }

    // ── getByUserId() ─────────────────────────────────────────────────

    @Test
    @DisplayName("getByUserId()는 userId로 알림 목록을 반환한다")
    void getByUserId_returnsNotificationList() {
        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of(notification));

        List<Notification> result = notificationService.getByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(notification);
    }

    @Test
    @DisplayName("getByUserId()는 알림이 없으면 빈 리스트를 반환한다")
    void getByUserId_noNotifications_returnsEmptyList() {
        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of());

        List<Notification> result = notificationService.getByUserId(1L);

        assertThat(result).isEmpty();
    }

    // ── getByIdAndUserId() ────────────────────────────────────────────

    @Test
    @DisplayName("getByIdAndUserId()는 존재하는 알림을 반환한다")
    void getByIdAndUserId_existingNotification_returnsNotification() {
        given(notificationRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(notification));

        Notification result = notificationService.getByIdAndUserId(10L, 1L);

        assertThat(result).isSameAs(notification);
    }

    @Test
    @DisplayName("getByIdAndUserId()는 존재하지 않으면 NotificationNotFoundException을 던진다")
    void getByIdAndUserId_notFound_throwsNotificationNotFoundException() {
        given(notificationRepository.findByIdAndUserId(99L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.getByIdAndUserId(99L, 1L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    // ── markAsRead() ──────────────────────────────────────────────────

    @Test
    @DisplayName("markAsRead()는 해당 알림의 isRead를 true로 변경한다")
    void markAsRead_setsIsReadToTrue() {
        given(notificationRepository.findByIdAndUserId(10L, 1L)).willReturn(Optional.of(notification));

        notificationService.markAsRead(10L, 1L);

        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("markAsRead()는 존재하지 않는 알림이면 NotificationNotFoundException을 던진다")
    void markAsRead_notFound_throwsNotificationNotFoundException() {
        given(notificationRepository.findByIdAndUserId(99L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L, 1L))
                .isInstanceOf(NotificationNotFoundException.class);
    }

    // ── markAllAsRead() ───────────────────────────────────────────────

    @Test
    @DisplayName("markAllAsRead()는 userId의 모든 알림을 읽음 처리한다")
    void markAllAsRead_marksAllNotificationsAsRead() {
        Notification other = Notification.create(new NotificationCreateCommand(1L, "다른 알림"));
        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(notification, other));

        notificationService.markAllAsRead(1L);

        assertThat(notification.isRead()).isTrue();
        assertThat(other.isRead()).isTrue();
    }

    @Test
    @DisplayName("markAllAsRead()는 알림이 없어도 예외를 던지지 않는다")
    void markAllAsRead_noNotifications_doesNotThrow() {
        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L)).willReturn(List.of());

        notificationService.markAllAsRead(1L);

        verify(notificationRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }
}
