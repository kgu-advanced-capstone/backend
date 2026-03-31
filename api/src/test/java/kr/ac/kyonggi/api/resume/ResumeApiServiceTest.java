package kr.ac.kyonggi.api.resume;

import kr.ac.kyonggi.api.resume.dto.ResumeResponse;
import kr.ac.kyonggi.common.exception.ResumeNotFoundException;
import kr.ac.kyonggi.domain.experience.Experience;
import kr.ac.kyonggi.domain.experience.ExperienceCreateCommand;
import kr.ac.kyonggi.domain.experience.ExperienceService;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectCreateCommand;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectMemberCreateCommand;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.resume.Resume;
import kr.ac.kyonggi.domain.resume.ResumedExperience;
import kr.ac.kyonggi.domain.resume.ResumeAiClient;
import kr.ac.kyonggi.domain.resume.ResumedExperienceRepository;
import kr.ac.kyonggi.domain.resume.ResumeService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import kr.ac.kyonggi.domain.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeApiServiceTest {

    @Mock private ResumeService resumeService;
    @Mock private UserService userService;
    @Mock private ProjectService projectService;
    @Mock private ExperienceService experienceService;
    @Mock private ResumedExperienceRepository resumedExperienceRepository;
    @Mock private ResumeAiClient resumeAiClient;

    @InjectMocks
    private ResumeApiService resumeApiService;

    private static final String EMAIL = "test@test.com";
    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 10L;

    private User user;
    private Project project;
    private ProjectMember member;

    @BeforeEach
    void setUp() {
        user = User.create(new UserCreateCommand(EMAIL, "pw", "홍길동", null));
        ReflectionTestUtils.setField(user, "id", USER_ID);

        project = Project.create(new ProjectCreateCommand(
                "테스트 프로젝트", "프로젝트 설명", "백엔드", List.of("Java", "Spring"), 4,
                LocalDate.of(2026, 12, 31), USER_ID
        ));
        ReflectionTestUtils.setField(project, "id", PROJECT_ID);

        member = ProjectMember.of(new ProjectMemberCreateCommand(PROJECT_ID, USER_ID));
    }

    // ── getResume() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getResume()는 이력서가 없으면 ResumeNotFoundException을 던진다")
    void getResume_throwsResumeNotFoundException_whenNotExists() {
        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> resumeApiService.getResume(EMAIL))
                .isInstanceOf(ResumeNotFoundException.class);
    }

    @Test
    @DisplayName("getResume()는 이력서가 있으면 ResumeResponse를 반환한다")
    void getResume_returnsResumeResponse_whenExists() {
        Resume mockResume = mock(Resume.class);
        when(mockResume.getId()).thenReturn(1L);
        when(mockResume.getGeneratedAt()).thenReturn(LocalDateTime.now());

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.of(mockResume));
        given(resumedExperienceRepository.findByResumeId(1L)).willReturn(List.of());

        ResumeResponse result = resumeApiService.getResume(EMAIL);

        assertThat(result.basicInfo().email()).isEqualTo(EMAIL);
        assertThat(result.summarizedExperiences()).isEmpty();
    }

    // ── generate() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("generate()는 경험 기록이 있으면 content를 포함하여 AI를 호출한다")
    void generate_passesExperienceContent_toGemini_whenExists() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(USER_ID, PROJECT_ID, "로그인 기능을 구현했습니다."));

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectService.getMembershipsOf(USER_ID)).willReturn(List.of(member));
        given(projectService.getAllByIds(List.of(PROJECT_ID))).willReturn(List.of(project));
        given(experienceService.findByProjectIdsAndUserId(List.of(PROJECT_ID), USER_ID))
                .willReturn(Map.of(PROJECT_ID, experience));
        given(resumeAiClient.generateKeyPoints(any(), any(), any(), any(), any()))
                .willReturn(List.of("키포인트1", "키포인트2"));
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(resumeService.save(any(Resume.class))).willAnswer(inv -> {
            Resume r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 1L);
            return r;
        });

        resumeApiService.generate(EMAIL);

        verify(resumeAiClient).generateKeyPoints(
                "테스트 프로젝트", "프로젝트 설명", "백엔드",
                List.of("Java", "Spring"), "로그인 기능을 구현했습니다."
        );
    }

    @Test
    @DisplayName("generate()는 경험 기록이 없으면 content를 null로 AI를 호출한다")
    void generate_passesNullContent_toGemini_whenNoExperience() {
        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectService.getMembershipsOf(USER_ID)).willReturn(List.of(member));
        given(projectService.getAllByIds(List.of(PROJECT_ID))).willReturn(List.of(project));
        given(experienceService.findByProjectIdsAndUserId(List.of(PROJECT_ID), USER_ID))
                .willReturn(Map.of());
        given(resumeAiClient.generateKeyPoints(any(), any(), any(), any(), any()))
                .willReturn(List.of("키포인트1"));
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(resumeService.save(any(Resume.class))).willAnswer(inv -> {
            Resume r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 1L);
            return r;
        });

        resumeApiService.generate(EMAIL);

        verify(resumeAiClient).generateKeyPoints(
                "테스트 프로젝트", "프로젝트 설명", "백엔드",
                List.of("Java", "Spring"), null
        );
    }

    @Test
    @DisplayName("generate()는 이력서가 없으면 새로 생성하고 experiences를 별도로 저장한다")
    void generate_createsNewResume_whenNoExistingResume() {
        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectService.getMembershipsOf(USER_ID)).willReturn(List.of(member));
        given(projectService.getAllByIds(List.of(PROJECT_ID))).willReturn(List.of(project));
        given(experienceService.findByProjectIdsAndUserId(List.of(PROJECT_ID), USER_ID))
                .willReturn(Map.of());
        given(resumeAiClient.generateKeyPoints(any(), any(), any(), any(), any()))
                .willReturn(List.of("키포인트1", "키포인트2"));
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(resumeService.save(any(Resume.class))).willAnswer(inv -> {
            Resume r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 1L);
            return r;
        });

        resumeApiService.generate(EMAIL);

        ArgumentCaptor<List<ResumedExperience>> captor = ArgumentCaptor.forClass(List.class);
        verify(resumedExperienceRepository).saveAll(captor.capture());
        List<ResumedExperience> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getProjectTitle()).isEqualTo("테스트 프로젝트");
        assertThat(saved.get(0).getKeyPoints()).containsExactly("키포인트1", "키포인트2");
    }

    @Test
    @DisplayName("generate()는 이력서가 이미 있으면 기존 experiences를 삭제하고 새로 저장한다")
    void generate_updatesExistingResume() {
        Resume existingResume = Resume.createFor(USER_ID);
        ReflectionTestUtils.setField(existingResume, "id", 5L);

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectService.getMembershipsOf(USER_ID)).willReturn(List.of(member));
        given(projectService.getAllByIds(List.of(PROJECT_ID))).willReturn(List.of(project));
        given(experienceService.findByProjectIdsAndUserId(List.of(PROJECT_ID), USER_ID))
                .willReturn(Map.of());
        given(resumeAiClient.generateKeyPoints(any(), any(), any(), any(), any()))
                .willReturn(List.of("새 키포인트"));
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.of(existingResume));
        given(resumeService.save(any(Resume.class))).willReturn(existingResume);

        resumeApiService.generate(EMAIL);

        verify(resumedExperienceRepository).deleteByResumeId(5L);
        verify(resumedExperienceRepository).saveAll(any());
    }

    @Test
    @DisplayName("generate()는 참가 프로젝트가 없으면 빈 experiences로 저장한다")
    void generate_emptyMemberships_savesResumeWithNoExperiences() {
        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectService.getMembershipsOf(USER_ID)).willReturn(List.of());
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(resumeService.save(any(Resume.class))).willAnswer(inv -> {
            Resume r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", 1L);
            return r;
        });

        resumeApiService.generate(EMAIL);

        ArgumentCaptor<List<ResumedExperience>> captor = ArgumentCaptor.forClass(List.class);
        verify(resumedExperienceRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).isEmpty();
        verify(resumeAiClient, never()).generateKeyPoints(any(), any(), any(), any(), any());
    }
}
