package kr.ac.kyonggi.api.experience;

import kr.ac.kyonggi.api.experience.dto.AiSummaryResponse;
import kr.ac.kyonggi.api.experience.dto.ExperienceRequest;
import kr.ac.kyonggi.api.experience.dto.ExperienceResponse;
import kr.ac.kyonggi.common.exception.ForbiddenException;
import kr.ac.kyonggi.domain.experience.Experience;
import kr.ac.kyonggi.domain.experience.ExperienceCreateCommand;
import kr.ac.kyonggi.domain.experience.ExperienceService;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectCreateCommand;
import kr.ac.kyonggi.domain.project.ProjectMemberRepository;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import kr.ac.kyonggi.domain.user.UserService;
import kr.ac.kyonggi.domain.experience.ExperienceSummarizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExperienceApiServiceTest {

    @Mock private ExperienceService experienceService;
    @Mock private ProjectService projectService;
    @Mock private UserService userService;
    @Mock private ProjectMemberRepository projectMemberRepository;
    @Mock private ExperienceSummarizer experienceSummarizer;

    @InjectMocks
    private ExperienceApiService experienceApiService;

    private static final String EMAIL = "test@test.com";
    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 10L;

    private User user;
    private Project project;
    private Experience experience;

    @BeforeEach
    void setUp() {
        user = User.create(new UserCreateCommand(EMAIL, "pw", "홍길동", null));
        ReflectionTestUtils.setField(user, "id", USER_ID);

        project = Project.create(new ProjectCreateCommand("프로젝트 제목", "설명", "카테고리", List.of("Java"), 5, null, USER_ID));
        ReflectionTestUtils.setField(project, "id", PROJECT_ID);

        experience = Experience.create(new ExperienceCreateCommand(USER_ID, PROJECT_ID, "로그인 기능을 구현했습니다."));
        ReflectionTestUtils.setField(experience, "id", 100L);
    }

    // ── getByProject() ────────────────────────────────────────────────

    @Test
    @DisplayName("getByProject()는 멤버인 사용자의 경험 목록을 ExperienceResponse로 반환한다")
    void getByProject_returnsList_forMember() {
        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectMemberRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).willReturn(true);
        given(experienceService.getByProjectIdAndUserId(PROJECT_ID, USER_ID)).willReturn(List.of(experience));

        List<ExperienceResponse> result = experienceApiService.getByProject(PROJECT_ID, EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(100L);
        assertThat(result.get(0).content()).isEqualTo("로그인 기능을 구현했습니다.");
    }

    @Test
    @DisplayName("getByProject()는 프로젝트 멤버가 아니면 ForbiddenException을 던진다")
    void getByProject_throwsForbiddenException_forNonMember() {
        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectMemberRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).willReturn(false);

        assertThatThrownBy(() -> experienceApiService.getByProject(PROJECT_ID, EMAIL))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── upsert() ──────────────────────────────────────────────────────

    @Test
    @DisplayName("upsert()는 경험이 없으면 새로 생성하고 반환한다")
    void upsert_createsNewExperience_whenNotExists() {
        ExperienceRequest request = new ExperienceRequest("새로운 경험 내용");

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectMemberRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).willReturn(true);
        given(experienceService.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).willReturn(Optional.empty());
        given(experienceService.save(any(Experience.class))).willAnswer(inv -> inv.getArgument(0));

        ExperienceResponse result = experienceApiService.upsert(PROJECT_ID, request, EMAIL);

        assertThat(result.content()).isEqualTo("새로운 경험 내용");
    }

    @Test
    @DisplayName("upsert()는 경험이 이미 있으면 content를 수정하고 반환한다")
    void upsert_updatesContent_whenAlreadyExists() {
        ExperienceRequest request = new ExperienceRequest("수정된 경험 내용");

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectMemberRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).willReturn(true);
        given(experienceService.findByProjectIdAndUserId(PROJECT_ID, USER_ID)).willReturn(Optional.of(experience));
        given(experienceService.save(any(Experience.class))).willAnswer(inv -> inv.getArgument(0));

        ExperienceResponse result = experienceApiService.upsert(PROJECT_ID, request, EMAIL);

        assertThat(result.content()).isEqualTo("수정된 경험 내용");
    }

    @Test
    @DisplayName("upsert()는 멤버가 아니면 ForbiddenException을 던진다")
    void upsert_throwsForbiddenException_forNonMember() {
        ExperienceRequest request = new ExperienceRequest("경험 내용");

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectMemberRepository.existsByProjectIdAndUserId(PROJECT_ID, USER_ID)).willReturn(false);

        assertThatThrownBy(() -> experienceApiService.upsert(PROJECT_ID, request, EMAIL))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── summarize() ───────────────────────────────────────────────────

    @Test
    @DisplayName("summarize()는 본인 경험에 AI 요약을 생성하고 AiSummaryResponse를 반환한다")
    void summarize_generatesAiSummary_forOwner() {
        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(experienceService.getById(100L)).willReturn(experience);
        given(projectService.getById(PROJECT_ID)).willReturn(project);
        given(experienceSummarizer.summarize(
                project.getTitle(),
                project.getDescription(),
                project.getCategory(),
                project.getSkills(),
                experience.getContent()
        )).willReturn("JWT 기반 로그인 인증 시스템 구축");
        given(experienceService.save(any(Experience.class))).willAnswer(inv -> inv.getArgument(0));

        AiSummaryResponse result = experienceApiService.summarize(100L, EMAIL);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.aiSummary()).isEqualTo("JWT 기반 로그인 인증 시스템 구축");
    }

    @Test
    @DisplayName("summarize()는 본인 경험이 아니면 ForbiddenException을 던진다")
    void summarize_throwsForbiddenException_forNonOwner() {
        User other = User.create(new UserCreateCommand("other@test.com", "pw", "타인", null));
        ReflectionTestUtils.setField(other, "id", 99L);

        given(userService.getByEmail("other@test.com")).willReturn(other);
        given(experienceService.getById(100L)).willReturn(experience);

        assertThatThrownBy(() -> experienceApiService.summarize(100L, "other@test.com"))
                .isInstanceOf(ForbiddenException.class);
    }
}
