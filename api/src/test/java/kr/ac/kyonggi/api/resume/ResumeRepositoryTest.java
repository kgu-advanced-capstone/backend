package kr.ac.kyonggi.api.resume;

import kr.ac.kyonggi.api.config.JpaTestConfig;
import kr.ac.kyonggi.domain.resume.Resume;
import kr.ac.kyonggi.domain.resume.ResumedExperience;
import kr.ac.kyonggi.domain.resume.ResumedExperienceRepository;
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

    @Autowired
    private ResumedExperienceRepository resumedExperienceRepository;

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
    @DisplayName("ResumedExperienceRepository로 resumeId별 경험을 저장하고 조회한다")
    void resumedExperience_saveAndFindByResumeId() {
        Resume resume = resumeRepository.save(Resume.createFor(2L));

        resumedExperienceRepository.saveAll(List.of(
                ResumedExperience.of(resume.getId(), 10L, "AI 기반 프로젝트", List.of("JWT 인증 구현", "CI/CD 파이프라인 구축"), List.of("Java", "Spring"))
        ));

        List<ResumedExperience> found = resumedExperienceRepository.findByResumeId(resume.getId());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getProjectTitle()).isEqualTo("AI 기반 프로젝트");
        assertThat(found.get(0).getKeyPoints())
                .containsExactlyInAnyOrder("JWT 인증 구현", "CI/CD 파이프라인 구축");
    }

    @Test
    @DisplayName("deleteByResumeId()는 해당 resumeId의 경험을 모두 삭제한다")
    void resumedExperience_deleteByResumeId() {
        Resume resume = resumeRepository.save(Resume.createFor(3L));
        resumedExperienceRepository.saveAll(List.of(
                ResumedExperience.of(resume.getId(), 10L, "프로젝트A", List.of("포인트1"), List.of()),
                ResumedExperience.of(resume.getId(), 20L, "프로젝트B", List.of("포인트2"), List.of())
        ));

        resumedExperienceRepository.deleteByResumeId(resume.getId());

        assertThat(resumedExperienceRepository.findByResumeId(resume.getId())).isEmpty();
    }
}
