package kr.ac.kyonggi.api.notification;

import kr.ac.kyonggi.api.notification.dto.NotificationResponse;
import kr.ac.kyonggi.domain.notification.NotificationService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationApiService {

    private final NotificationService notificationService;
    private final UserService userService;

    public List<NotificationResponse> getNotifications(String userEmail) {
        User user = userService.getByEmail(userEmail);
        return notificationService.getByUserId(user.getId()).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void markAsRead(Long notificationId, String userEmail) {
        User user = userService.getByEmail(userEmail);
        notificationService.markAsRead(notificationId, user.getId());
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = userService.getByEmail(userEmail);
        notificationService.markAllAsRead(user.getId());
    }
}
