package kr.ac.kyonggi.domain.notification;

import kr.ac.kyonggi.common.exception.NotificationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void create(Notification notification) {
        notificationRepository.save(notification);
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
