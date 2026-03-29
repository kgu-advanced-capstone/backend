package kr.ac.kyonggi.api.notification;

import kr.ac.kyonggi.api.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationApiService notificationApiService;

    @GetMapping
    public List<NotificationResponse> getNotifications(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return notificationApiService.getNotifications(userDetails.getUsername());
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        notificationApiService.markAllAsRead(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        notificationApiService.markAsRead(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
