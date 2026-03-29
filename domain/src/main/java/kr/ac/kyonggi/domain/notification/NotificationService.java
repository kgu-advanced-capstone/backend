package kr.ac.kyonggi.domain.notification;

import java.util.List;

public interface NotificationService {

    Notification create(Notification notification);

    List<Notification> getByUserId(Long userId);

    Notification getByIdAndUserId(Long id, Long userId);

    void markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);
}
