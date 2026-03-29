package kr.ac.kyonggi.api.resume;

import kr.ac.kyonggi.api.config.JpaTestConfig;
import kr.ac.kyonggi.domain.resume.Resume;
import kr.ac.kyonggi.domain.resume.ResumedExperience;
import kr.ac.kyonggi.domain.resume.ResumeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
class ResumeRepositoryTest {

    @Autowired
    private ResumeRepository resumeRepository;

    @Test
    @DisplayName("findByUserId()는 존재하는 userId면 Resume를 반환한다")
    void findByUserId_returnsResume_whenExists() {
        Resume resume = Resume.createFor(1L);
        resumeRepository.save(resume);

        Optional<Resume> result = resumeRepository.findByUserId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByUserId()는 존재하지 않는 userId면 Optional.empty()를 반환한다")
    void findByUserId_returnsEmpty_whenNotExists() {
        Optional<Resume> result = resumeRepository.findByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("save()는 Resume와 연관된 experiences 및 keyPoints를 함께 영속화한다")
    void save_persistsExperiencesAndKeyPoints() {
        Resume resume = Resume.createFor(2L);
        resume.updateExperiences(List.of(
                ResumedExperience.of(10L, "AI 기반 프로젝트", List.of("JWT 인증 구현", "CI/CD 파이프라인 구축"))
        ));
        resumeRepository.save(resume);

        Optional<Resume> found = resumeRepository.findByUserId(2L);

        assertThat(found).isPresent();
        assertThat(found.get().getExperiences()).hasSize(1);
        assertThat(found.get().getExperiences().get(0).getProjectTitle()).isEqualTo("AI 기반 프로젝트");
        assertThat(found.get().getExperiences().get(0).getKeyPoints())
                .containsExactlyInAnyOrder("JWT 인증 구현", "CI/CD 파이프라인 구축");
    }
}
