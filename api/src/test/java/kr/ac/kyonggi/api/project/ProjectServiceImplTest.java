package kr.ac.kyonggi.api.project;

import kr.ac.kyonggi.api.config.JpaTestConfig;
import kr.ac.kyonggi.common.exception.AlreadyAppliedException;
import kr.ac.kyonggi.common.exception.ForbiddenException;
import kr.ac.kyonggi.common.exception.ProjectFullException;
import kr.ac.kyonggi.common.exception.ProjectNotRecruitingException;
import kr.ac.kyonggi.common.exception.ProjectNotFoundException;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectCreateCommand;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectMemberRepository;
import kr.ac.kyonggi.domain.project.ProjectRepository;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.project.ProjectServiceImpl;
import kr.ac.kyonggi.domain.project.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({JpaTestConfig.class, ProjectServiceImpl.class})
@ActiveProfiles("test")
class ProjectServiceImplTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    @Autowired
    private TestEntityManager em;

    private Project project;

    @BeforeEach
    void setUp() {
        project = em.persist(Project.create(new ProjectCreateCommand(
                "테스트 프로젝트", "설명", "백엔드", List.of("Java"), 4,
                LocalDate.of(2026, 12, 31), 1L
        )));
        em.flush();
    }

    // ── create() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("create()는 프로젝트를 저장하고 ID가 부여된 엔티티를 반환한다")
    void create_savesAndReturnsProject() {
        Project newProject = Project.create(new ProjectCreateCommand(
                "새 프로젝트", "설명", "프론트엔드", List.of("React"), 3,
                LocalDate.of(2026, 12, 31), 1L
        ));

        Project saved = projectService.create(newProject);

        assertThat(saved.getId()).isNotNull();
        assertThat(projectRepository.findById(saved.getId())).isPresent();
    }

    // ── getById() ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getById()는 존재하는 ID면 프로젝트를 반환한다")
    void getById_existingId_returnsProject() {
        Project result = projectService.getById(project.getId());

        assertThat(result.getTitle()).isEqualTo("테스트 프로젝트");
    }

    @Test
    @DisplayName("getById()는 존재하지 않는 ID면 ProjectNotFoundException을 던진다")
    void getById_nonExistingId_throwsProjectNotFoundException() {
        assertThatThrownBy(() -> projectService.getById(99L))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    // ── apply() ───────────────────────────────────────────────────────

    @Test
    @DisplayName("apply()는 프로젝트가 존재하지 않으면 ProjectNotFoundException을 던진다")
    void apply_projectNotFound_throwsProjectNotFoundException() {
        assertThatThrownBy(() -> projectService.apply(99L, 2L))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    @DisplayName("apply()는 이미 참가 신청한 경우 AlreadyAppliedException을 던진다")
    void apply_alreadyApplied_throwsAlreadyAppliedException() {
        projectService.apply(project.getId(), 2L);

        assertThatThrownBy(() -> projectService.apply(project.getId(), 2L))
                .isInstanceOf(AlreadyAppliedException.class);
    }

    @Test
    @DisplayName("apply()는 프로젝트가 RECRUITING 상태가 아니면 ProjectNotRecruitingException을 던진다")
    void apply_notRecruiting_throwsProjectNotRecruitingException() {
        project.updateStatus(ProjectStatus.IN_PROGRESS);
        em.flush();

        assertThatThrownBy(() -> projectService.apply(project.getId(), 2L))
                .isInstanceOf(ProjectNotRecruitingException.class);
    }

    @Test
    @DisplayName("apply()는 프로젝트 인원이 가득 찬 경우 ProjectFullException을 던진다")
    void apply_projectFull_throwsProjectFullException() {
        for (long userId = 10L; userId < 14L; userId++) { // maxMembers = 4
            projectService.apply(project.getId(), userId);
        }

        assertThatThrownBy(() -> projectService.apply(project.getId(), 99L))
                .isInstanceOf(ProjectFullException.class);
    }

    @Test
    @DisplayName("apply()는 신규 신청 시 currentMemberCount를 1 증가시키고 ProjectMember를 저장한 뒤 Project를 반환한다")
    void apply_success_savesProjectMemberAndIncrementsCount() {
        Project result = projectService.apply(project.getId(), 2L);

        assertThat(result.getCurrentMemberCount()).isEqualTo(1);
        assertThat(projectMemberRepository.existsByProjectIdAndUserId(project.getId(), 2L)).isTrue();
    }

    // ── updateStatus() ────────────────────────────────────────────────

    @Test
    @DisplayName("updateStatus()는 작성자가 아닌 경우 ForbiddenException을 던진다")
    void updateStatus_notAuthor_throwsForbiddenException() {
        assertThatThrownBy(() -> projectService.updateStatus(project.getId(), 99L, ProjectStatus.IN_PROGRESS))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("updateStatus()는 작성자인 경우 상태를 변경한다")
    void updateStatus_asAuthor_updatesStatus() {
        projectService.updateStatus(project.getId(), 1L, ProjectStatus.IN_PROGRESS);
        em.flush();
        em.clear();

        Project updated = projectRepository.findById(project.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    // ── getMemberCount() ──────────────────────────────────────────────

    @Test
    @DisplayName("getMemberCount()는 프로젝트에 참가한 멤버 수를 반환한다")
    void getMemberCount_returnsCorrectCount() {
        projectService.apply(project.getId(), 2L);
        projectService.apply(project.getId(), 3L);

        assertThat(projectService.getMemberCount(project.getId())).isEqualTo(2L);
    }

    // ── getMembershipsOf() ────────────────────────────────────────────

    @Test
    @DisplayName("getMembershipsOf()는 유저가 참가한 ProjectMember 목록을 반환한다")
    void getMembershipsOf_returnsUserMemberships() {
        projectService.apply(project.getId(), 2L);

        List<ProjectMember> memberships = projectService.getMembershipsOf(2L);

        assertThat(memberships).hasSize(1);
        assertThat(memberships.get(0).getProjectId()).isEqualTo(project.getId());
    }

    // ── getParticipants() ─────────────────────────────────────────────

    @Test
    @DisplayName("getParticipants()는 해당 프로젝트의 ProjectMember 목록을 반환한다")
    void getParticipants_returnsProjectMembers() {
        projectService.apply(project.getId(), 2L);
        projectService.apply(project.getId(), 3L);

        List<ProjectMember> participants = projectService.getParticipants(project.getId());

        assertThat(participants).hasSize(2);
        assertThat(participants).extracting(ProjectMember::getProjectId)
                .containsOnly(project.getId());
    }
}
