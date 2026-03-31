package kr.ac.kyonggi.api.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.ac.kyonggi.domain.notification.Notification;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public record NotificationResponse(
        @Schema(description = "알림 고유 ID", example = "15")
        Long id,

        @Schema(description = "알림 메시지", example = "스마트 캠퍼스 앱 개발 프로젝트에 새 팀원이 지원했습니다.")
        String message,

        @Schema(description = "알림 수신 시각 (오전/오후 hh:mm)", example = "오전 09:30")
        String time,

        @Schema(description = "읽음 여부", example = "false")
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
