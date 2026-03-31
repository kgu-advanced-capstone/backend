package kr.ac.kyonggi.domain.project;

import kr.ac.kyonggi.common.exception.AlreadyAppliedException;
import kr.ac.kyonggi.common.exception.ForbiddenException;
import kr.ac.kyonggi.common.exception.ProjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private Project project;

    @BeforeEach
    void setUp() {
        project = Project.create(new ProjectCreateCommand(
                "테스트 프로젝트", "설명", "백엔드", List.of("Java"), 4,
                LocalDate.of(2026, 12, 31), 1L
        ));
        ReflectionTestUtils.setField(project, "id", 10L);
    }

    // ── create() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("create()는 repository.save()를 호출하고 저장된 프로젝트를 반환한다")
    void create_savesAndReturnsProject() {
        given(projectRepository.save(project)).willReturn(project);

        Project result = projectService.create(project);

        assertThat(result).isSameAs(project);
        verify(projectRepository).save(project);
    }

    // ── getById() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getById()는 존재하는 ID면 프로젝트를 반환한다")
    void getById_existingId_returnsProject() {
        given(projectRepository.findById(10L)).willReturn(Optional.of(project));

        Project result = projectService.getById(10L);

        assertThat(result).isSameAs(project);
    }

    @Test
    @DisplayName("getById()는 존재하지 않는 ID면 ProjectNotFoundException을 던진다")
    void getById_nonExistingId_throwsProjectNotFoundException() {
        given(projectRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getById(99L))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    // ── search() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("search()는 필터와 페이지 조건을 repository에 위임하고 결과를 반환한다")
    void search_delegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> page = new PageImpl<>(List.of(project));
        given(projectRepository.findWithFilters("백엔드", "테스트", pageable)).willReturn(page);

        Page<Project> result = projectService.search("백엔드", "테스트", pageable);

        assertThat(result.getContent()).containsExactly(project);
    }

    // ── apply() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("apply()는 이미 참가 신청한 경우 AlreadyAppliedException을 던진다")
    void apply_alreadyApplied_throwsAlreadyAppliedException() {
        given(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).willReturn(true);

        assertThatThrownBy(() -> projectService.apply(10L, 2L))
                .isInstanceOf(AlreadyAppliedException.class);
    }

    @Test
    @DisplayName("apply()는 신규 신청 시 ProjectMember를 생성하고 반환한다")
    void apply_newApplication_returnsProjectMember() {
        given(projectMemberRepository.existsByProjectIdAndUserId(10L, 2L)).willReturn(false);

        ProjectMember saved = ProjectMember.of(new ProjectMemberCreateCommand(10L, 2L));
        given(projectMemberRepository.save(any(ProjectMember.class))).willReturn(saved);

        ProjectMember result = projectService.apply(10L, 2L);

        assertThat(result.getProjectId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(2L);
        verify(projectMemberRepository).save(any(ProjectMember.class));
    }

    // ── updateStatus() ────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus()는 작성자가 아닌 경우 ForbiddenException을 던진다")
    void updateStatus_notAuthor_throwsForbiddenException() {
        given(projectRepository.findById(10L)).willReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateStatus(10L, 99L, ProjectStatus.IN_PROGRESS))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("updateStatus()는 작성자인 경우 상태를 변경한다")
    void updateStatus_asAuthor_updatesStatus() {
        given(projectRepository.findById(10L)).willReturn(Optional.of(project));

        projectService.updateStatus(10L, 1L, ProjectStatus.IN_PROGRESS);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    // ── getMemberCount() ──────────────────────────────────────────────

    @Test
    @DisplayName("getMemberCount()는 projectMemberRepository.countByProjectId()를 위임하여 반환한다")
    void getMemberCount_returnsCountFromRepository() {
        given(projectMemberRepository.countByProjectId(10L)).willReturn(3L);

        long count = projectService.getMemberCount(10L);

        assertThat(count).isEqualTo(3L);
    }

    // ── getMembershipsOf() ────────────────────────────────────────────

    @Test
    @DisplayName("getMembershipsOf()는 유저의 ProjectMember 목록을 반환한다")
    void getMembershipsOf_returnsUserMemberships() {
        ProjectMember member = ProjectMember.of(new ProjectMemberCreateCommand(10L, 1L));
        given(projectMemberRepository.findByUserId(1L)).willReturn(List.of(member));

        List<ProjectMember> result = projectService.getMembershipsOf(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectId()).isEqualTo(10L);
    }
}
