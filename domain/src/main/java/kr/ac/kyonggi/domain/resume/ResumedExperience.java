package kr.ac.kyonggi.domain.resume;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "resume_experiences")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumedExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long resumeId;

    private Long projectId;

    private String projectTitle;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "resume_experience_key_points",
            joinColumns = @JoinColumn(name = "experience_id")
    )
    @Column(name = "key_point")
    private List<String> keyPoints = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "resume_experience_skills",
            joinColumns = @JoinColumn(name = "experience_id")
    )
    @Column(name = "skill")
    private List<String> skills = new ArrayList<>();

    public static ResumedExperience of(Long resumeId, Long projectId, String projectTitle, List<String> keyPoints, List<String> skills) {
        ResumedExperience experience = new ResumedExperience();
        experience.resumeId = resumeId;
        experience.projectId = projectId;
        experience.projectTitle = projectTitle;
        experience.keyPoints = new ArrayList<>(keyPoints);
        experience.skills = skills != null ? new ArrayList<>(skills) : new ArrayList<>();
        return experience;
    }
}
