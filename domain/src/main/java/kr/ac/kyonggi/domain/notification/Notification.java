package kr.ac.kyonggi.domain.notification;

import jakarta.persistence.*;
import kr.ac.kyonggi.domain.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean isRead = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Notification create(NotificationCreateCommand command) {
        return new Notification(command);
    }

    private Notification(NotificationCreateCommand command) {
        this.user = command.user();
        this.message = command.message();
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
