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

    private static final String DEFAULT_COVER_LETTER_TITLE = "자기소개서";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long userId;

    @Column(name = "cover_letter_title", nullable = false, length = 40)
    private String coverLetterTitle;

    @Column(name = "cover_letter_content", nullable = false, length = 4000)
    private String coverLetterContent;

    private LocalDateTime generatedAt;

    public static Resume createFor(Long userId) {
        Resume resume = new Resume();
        resume.userId = userId;
        resume.coverLetterTitle = DEFAULT_COVER_LETTER_TITLE;
        resume.coverLetterContent = "";
        resume.generatedAt = LocalDateTime.now();
        return resume;
    }

    public void updateCoverLetter(String title, String content) {
        this.coverLetterTitle = normalizeCoverLetterTitle(title);
        this.coverLetterContent = normalizeCoverLetterContent(content);
    }

    public void markGenerated() {
        this.generatedAt = LocalDateTime.now();
    }

    private String normalizeCoverLetterTitle(String title) {
        if (title == null || title.isBlank()) {
            return DEFAULT_COVER_LETTER_TITLE;
        }
        return title.trim();
    }

    private String normalizeCoverLetterContent(String content) {
        if (content == null) {
            return "";
        }
        return content.trim();
    }
}
