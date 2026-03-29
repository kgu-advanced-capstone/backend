package kr.ac.kyonggi.domain.experience;

import jakarta.persistence.*;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.user.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "experiences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "project_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Experience create(ExperienceCreateCommand command) {
        return new Experience(command);
    }

    private Experience(ExperienceCreateCommand command) {
        this.user = command.user();
        this.project = command.project();
        this.content = command.content();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateAiSummary(String summary) {
        this.aiSummary = summary;
    }
}
