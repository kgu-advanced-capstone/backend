package kr.ac.kyonggi.domain.resume;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    private LocalDateTime generatedAt;

    public static Resume createFor(Long userId) {
        Resume resume = new Resume();
        resume.userId = userId;
        resume.generatedAt = LocalDateTime.now();
        return resume;
    }

    public void markGenerated() {
        this.generatedAt = LocalDateTime.now();
    }
}
