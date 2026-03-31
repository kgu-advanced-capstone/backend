package kr.ac.kyonggi.api.notification;

import kr.ac.kyonggi.domain.notification.Notification;
import kr.ac.kyonggi.domain.notification.NotificationCreateCommand;
import kr.ac.kyonggi.domain.notification.NotificationCreatedEvent;
import kr.ac.kyonggi.domain.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationCreatedEvent event) {
        try {
            Notification notification = Notification.create(
                    new NotificationCreateCommand(event.userId(), event.message()));
            notificationService.create(notification);
        } catch (DataAccessException e) {
            log.error("알림 생성 실패: userId={}, message={}", event.userId(), event.message(), e);
        }
    }
}
