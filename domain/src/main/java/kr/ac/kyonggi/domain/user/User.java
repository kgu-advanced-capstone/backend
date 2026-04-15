package kr.ac.kyonggi.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String profileImage;

    private String phone;

    private String github;

    private String blog;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static User create(UserCreateCommand command) {
        return new User(command);
    }

    private User(UserCreateCommand command) {
        this.email = command.email();
        this.password = command.password();
        this.name = command.name();
        this.profileImage = command.profileImage();
        this.phone = command.phone();
    }

    public void updateProfile(UpdateProfileCommand command) {
        if (command.name() != null) {
            this.name = command.name();
        }
        if (command.phone() != null) {
            this.phone = command.phone();
        }
        if (command.github() != null) {
            this.github = command.github();
        }
        if (command.blog() != null) {
            this.blog = command.blog();
        }
        if (command.profileImage() != null) {
            this.profileImage = command.profileImage();
        }
    }
}
