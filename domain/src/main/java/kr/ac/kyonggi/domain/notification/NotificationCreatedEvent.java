package kr.ac.kyonggi.domain.notification;

public record NotificationCreatedEvent(
        Long userId,
        String message
) {
}
