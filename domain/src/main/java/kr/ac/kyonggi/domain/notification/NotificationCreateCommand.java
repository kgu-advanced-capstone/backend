package kr.ac.kyonggi.domain.notification;

import kr.ac.kyonggi.domain.user.User;

public record NotificationCreateCommand(
        User user,
        String message
) {
}
