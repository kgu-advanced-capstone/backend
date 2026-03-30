package kr.ac.kyonggi.domain.notification;

import kr.ac.kyonggi.common.exception.NotificationNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Async
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(Notification notification) {
        try {
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("알림 생성 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<Notification> getByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Notification getByIdAndUserId(Long id, Long userId) {
        return notificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotificationNotFoundException("알림을 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        Notification notification = getByIdAndUserId(id, userId);
        notification.markAsRead();
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(Notification::markAsRead);
    }
}
