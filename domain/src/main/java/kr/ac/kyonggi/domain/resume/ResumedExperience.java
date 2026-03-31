package kr.ac.kyonggi.domain.resume;

import jakarta.persistence.*;
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

    public static ResumedExperience of(Long resumeId, Long projectId, String projectTitle, List<String> keyPoints) {
        ResumedExperience experience = new ResumedExperience();
        experience.resumeId = resumeId;
        experience.projectId = projectId;
        experience.projectTitle = projectTitle;
        experience.keyPoints = new ArrayList<>(keyPoints);
        return experience;
    }
}
