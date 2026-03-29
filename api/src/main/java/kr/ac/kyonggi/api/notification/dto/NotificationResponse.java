package kr.ac.kyonggi.api.notification.dto;

import kr.ac.kyonggi.domain.notification.Notification;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record NotificationResponse(
        Long id,
        String message,
        String time,
        boolean read
) {
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN);

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getMessage(),
                notification.getCreatedAt() != null
                        ? notification.getCreatedAt().format(TIME_FORMATTER)
                        : null,
                notification.isRead()
        );
    }
}
