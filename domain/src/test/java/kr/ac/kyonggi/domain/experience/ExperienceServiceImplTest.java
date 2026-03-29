package kr.ac.kyonggi.domain.experience;

import kr.ac.kyonggi.common.exception.ExperienceNotFoundException;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectCreateCommand;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExperienceServiceImplTest {

    @Mock
    private ExperienceRepository experienceRepository;

    @InjectMocks
    private ExperienceServiceImpl experienceService;

    private User user;
    private Project project;
    private Experience experience;

    @BeforeEach
    void setUp() {
        user = User.create(new UserCreateCommand("test@test.com", "pw", "홍길동", null));
        ReflectionTestUtils.setField(user, "id", 1L);

        project = Project.create(new ProjectCreateCommand(
                "테스트 프로젝트", "설명", "백엔드", List.of("Java"), 4,
                LocalDate.of(2026, 12, 31), user
        ));
        ReflectionTestUtils.setField(project, "id", 10L);

        experience = Experience.create(new ExperienceCreateCommand(user, project, "개발 내용입니다."));
        ReflectionTestUtils.setField(experience, "id", 5L);
    }

    // ── getByProjectIdAndUserId() ─────────────────────────────────────

    @Test
    @DisplayName("getByProjectIdAndUserId()는 경험이 존재하면 단일 요소 리스트를 반환한다")
    void getByProjectIdAndUserId_exists_returnsSingleElementList() {
        given(experienceRepository.findByProjectIdAndUserId(10L, 1L))
                .willReturn(Optional.of(experience));

        List<Experience> result = experienceService.getByProjectIdAndUserId(10L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isSameAs(experience);
    }

    @Test
    @DisplayName("getByProjectIdAndUserId()는 경험이 없으면 빈 리스트를 반환한다")
    void getByProjectIdAndUserId_notExists_returnsEmptyList() {
        given(experienceRepository.findByProjectIdAndUserId(10L, 1L))
                .willReturn(Optional.empty());

        List<Experience> result = experienceService.getByProjectIdAndUserId(10L, 1L);

        assertThat(result).isEmpty();
    }

    // ── findByProjectIdAndUserId() ────────────────────────────────────

    @Test
    @DisplayName("findByProjectIdAndUserId()는 repository에 위임하고 Optional을 반환한다")
    void findByProjectIdAndUserId_delegatesToRepository() {
        given(experienceRepository.findByProjectIdAndUserId(10L, 1L))
                .willReturn(Optional.of(experience));

        Optional<Experience> result = experienceService.findByProjectIdAndUserId(10L, 1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(experience);
    }

    // ── getById() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getById()는 존재하는 ID면 경험을 반환한다")
    void getById_existingId_returnsExperience() {
        given(experienceRepository.findById(5L)).willReturn(Optional.of(experience));

        Experience result = experienceService.getById(5L);

        assertThat(result).isSameAs(experience);
    }

    @Test
    @DisplayName("getById()는 존재하지 않는 ID면 ExperienceNotFoundException을 던진다")
    void getById_nonExistingId_throwsExperienceNotFoundException() {
        given(experienceRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> experienceService.getById(99L))
                .isInstanceOf(ExperienceNotFoundException.class);
    }

    // ── save() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("save()는 repository.save()를 호출하고 저장된 경험을 반환한다")
    void save_savesAndReturnsExperience() {
        given(experienceRepository.save(experience)).willReturn(experience);

        Experience result = experienceService.save(experience);

        assertThat(result).isSameAs(experience);
        verify(experienceRepository).save(experience);
    }
}
