package kr.ac.kyonggi.domain.resume;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeTest {

    @Test
    @DisplayName("createFor()는 userId를 저장하고 generatedAt을 초기화한다")
    void createFor_setsUserIdAndGeneratedAt() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        Resume resume = Resume.createFor(1L);

        assertThat(resume.getUserId()).isEqualTo(1L);
        assertThat(resume.getGeneratedAt()).isAfter(before);
        assertThat(resume.getExperiences()).isEmpty();
    }

    @Test
    @DisplayName("updateExperiences()는 기존 experiences를 모두 새 목록으로 교체한다")
    void updateExperiences_replacesAll() {
        Resume resume = Resume.createFor(1L);
        resume.updateExperiences(List.of(
                ResumedExperience.of(1L, "첫 번째 프로젝트", List.of("포인트A"))
        ));

        ResumedExperience newExp = ResumedExperience.of(2L, "두 번째 프로젝트", List.of("포인트B", "포인트C"));
        resume.updateExperiences(List.of(newExp));

        assertThat(resume.getExperiences()).hasSize(1);
        assertThat(resume.getExperiences().get(0).getProjectTitle()).isEqualTo("두 번째 프로젝트");
    }

    @Test
    @DisplayName("updateExperiences()는 generatedAt을 갱신한다")
    void updateExperiences_updatesGeneratedAt() throws InterruptedException {
        Resume resume = Resume.createFor(1L);
        LocalDateTime first = resume.getGeneratedAt();

        Thread.sleep(10);
        resume.updateExperiences(List.of(
                ResumedExperience.of(1L, "프로젝트", List.of("포인트"))
        ));

        assertThat(resume.getGeneratedAt()).isAfter(first);
    }
}
