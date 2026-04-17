package kr.ac.kyonggi.domain.experience;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiSummaryStatus aiSummaryStatus = AiSummaryStatus.NONE;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static Experience create(ExperienceCreateCommand command) {
        return new Experience(command);
    }

    private Experience(ExperienceCreateCommand command) {
        this.userId = command.userId();
        this.projectId = command.projectId();
        this.content = command.content();
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateAiSummary(String summary) {
        this.aiSummary = summary;
    }

    public void startSummarizing() {
        this.aiSummaryStatus = AiSummaryStatus.IN_PROGRESS;
    }

    public void completeSummarizing(String summary) {
        this.aiSummary = summary;
        this.aiSummaryStatus = AiSummaryStatus.COMPLETED;
    }

    public void failSummarizing() {
        this.aiSummaryStatus = AiSummaryStatus.FAILED;
    }

    public boolean isSummarizing() {
        return this.aiSummaryStatus == AiSummaryStatus.IN_PROGRESS;
    }
}
