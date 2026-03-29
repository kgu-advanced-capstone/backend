package kr.ac.kyonggi.domain.resume;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resumes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "resume_id")
    private List<ResumedExperience> experiences = new ArrayList<>();

    private LocalDateTime generatedAt;

    public static Resume createFor(Long userId) {
        Resume resume = new Resume();
        resume.userId = userId;
        resume.experiences = new ArrayList<>();
        resume.generatedAt = LocalDateTime.now();
        return resume;
    }

    public void updateExperiences(List<ResumedExperience> newExperiences) {
        this.experiences.clear();
        this.experiences.addAll(newExperiences);
        this.generatedAt = LocalDateTime.now();
    }
}
