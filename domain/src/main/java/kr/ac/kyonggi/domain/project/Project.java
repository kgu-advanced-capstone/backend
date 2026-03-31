package kr.ac.kyonggi.domain.project;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @ElementCollection
    @CollectionTable(name = "project_skills", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    @Column(nullable = false)
    private int maxMembers;

    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Project create(ProjectCreateCommand command) {
        return new Project(command);
    }

    private Project(ProjectCreateCommand command) {
        this.title = command.title();
        this.description = command.description();
        this.category = command.category();
        this.skills = command.skills() != null ? new ArrayList<>(command.skills()) : new ArrayList<>();
        this.maxMembers = command.maxMembers();
        this.deadline = command.deadline();
        this.status = ProjectStatus.RECRUITING;
        this.authorId = command.authorId();
    }

    public void updateStatus(ProjectStatus status) {
        this.status = status;
    }

    public boolean isAuthor(Long userId) {
        return authorId.equals(userId);
    }
}
