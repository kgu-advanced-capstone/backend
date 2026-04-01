package kr.ac.kyonggi.domain.project;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import kr.ac.kyonggi.common.exception.ProjectFullException;
import kr.ac.kyonggi.common.exception.ProjectNotRecruitingException;
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

    @Column(nullable = false)
    private int currentMemberCount = 0;

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

    public void addMember() {
        if (status != ProjectStatus.RECRUITING) {
            throw new ProjectNotRecruitingException("모집 중인 프로젝트가 아닙니다.");
        }
        if (currentMemberCount >= maxMembers) {
            throw new ProjectFullException("프로젝트 인원이 이미 가득 찼습니다.");
        }
        this.currentMemberCount++;
    }

    public void updateStatus(ProjectStatus status) {
        this.status = status;
    }

    public boolean isAuthor(Long userId) {
        return authorId.equals(userId);
    }
}
