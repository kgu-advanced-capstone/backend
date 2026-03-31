package kr.ac.kyonggi.api.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.kyonggi.api.notification.dto.NotificationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Notification", description = "알림 API")
public interface NotificationApi {

    @Operation(
            summary = "알림 목록 조회",
            description = "현재 사용자의 모든 알림을 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = NotificationResponse.class)))),
            @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content)
    })
    @GetMapping
    List<NotificationResponse> getNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "모든 알림 읽음 처리",
            description = "현재 사용자의 모든 알림을 읽음으로 표시합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponse(responseCode = "200", description = "처리 성공", content = @Content)
    @PatchMapping("/read-all")
    ResponseEntity<Void> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "단일 알림 읽음 처리",
            description = "특정 알림을 읽음으로 표시합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "처리 성공", content = @Content),
            @ApiResponse(responseCode = "404", description = "알림 없음", content = @Content)
    })
    @PatchMapping("/{id}/read")
    ResponseEntity<Void> markAsRead(
            @Parameter(description = "알림 ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails
    );
}
