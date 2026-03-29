package kr.ac.kyonggi.api.resume;

import kr.ac.kyonggi.api.resume.dto.ResumeResponse;
import kr.ac.kyonggi.common.exception.ResumeNotFoundException;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.resume.Resume;
import kr.ac.kyonggi.domain.resume.ResumeService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import kr.ac.kyonggi.infrastructure.external.GeminiResumeClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeApiServiceTest {

    @Mock
    private ResumeService resumeService;

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    @Mock
    private GeminiResumeClient geminiClient;

    @InjectMocks
    private ResumeApiService resumeApiService;

    private static final String EMAIL = "test@test.com";
    private static final Long USER_ID = 1L;

    // ── getResume() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getResume()는 이력서가 없으면 ResumeNotFoundException을 던진다")
    void getResume_throwsResumeNotFoundException_whenNotExists() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(USER_ID);
        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> resumeApiService.getResume(EMAIL))
                .isInstanceOf(ResumeNotFoundException.class);
    }

    @Test
    @DisplayName("getResume()는 이력서가 있으면 ResumeResponse를 반환한다")
    void getResume_returnsResumeResponse_whenExists() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(USER_ID);
        when(user.getEmail()).thenReturn(EMAIL);
        when(user.getName()).thenReturn("홍길동");
        when(user.getPhone()).thenReturn(null);
        when(user.getGithub()).thenReturn(null);
        when(user.getBlog()).thenReturn(null);
        when(user.getProfileImage()).thenReturn(null);

        Resume mockResume = mock(Resume.class);
        when(mockResume.getExperiences()).thenReturn(List.of());
        when(mockResume.getGeneratedAt()).thenReturn(LocalDateTime.now());

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.of(mockResume));

        ResumeResponse result = resumeApiService.getResume(EMAIL);

        assertThat(result.basicInfo().email()).isEqualTo(EMAIL);
        assertThat(result.summarizedExperiences()).isEmpty();
    }

    // ── generate() ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("generate()는 이력서가 없으면 새로 생성하고 저장한다")
    void generate_createsNewResume_whenNoExistingResume() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(USER_ID);

        Project project = mock(Project.class);
        when(project.getId()).thenReturn(10L);
        when(project.getTitle()).thenReturn("테스트 프로젝트");
        when(project.getDescription()).thenReturn("프로젝트 설명");
        when(project.getCategory()).thenReturn("백엔드");
        when(project.getSkills()).thenReturn(List.of("Java", "Spring"));

        ProjectMember member = mock(ProjectMember.class);
        when(member.getProject()).thenReturn(project);

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectService.getMembershipsOf(USER_ID)).willReturn(List.of(member));
        given(geminiClient.generateKeyPoints(any(), any(), any(), any()))
                .willReturn(List.of("키포인트1", "키포인트2"));
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(resumeService.save(any(Resume.class))).willAnswer(inv -> inv.getArgument(0));

        resumeApiService.generate(EMAIL);

        ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
        verify(resumeService).save(captor.capture());
        Resume saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getExperiences()).hasSize(1);
        assertThat(saved.getExperiences().get(0).getProjectTitle()).isEqualTo("테스트 프로젝트");
        assertThat(saved.getExperiences().get(0).getKeyPoints()).containsExactly("키포인트1", "키포인트2");
    }

    @Test
    @DisplayName("generate()는 이력서가 이미 있으면 experiences를 갱신한다")
    void generate_updatesExistingResume() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(USER_ID);

        Project project = mock(Project.class);
        when(project.getId()).thenReturn(10L);
        when(project.getTitle()).thenReturn("테스트 프로젝트");
        when(project.getDescription()).thenReturn("프로젝트 설명");
        when(project.getCategory()).thenReturn("백엔드");
        when(project.getSkills()).thenReturn(List.of("Java", "Spring"));

        ProjectMember member = mock(ProjectMember.class);
        when(member.getProject()).thenReturn(project);

        Resume existingResume = Resume.createFor(USER_ID);

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectService.getMembershipsOf(USER_ID)).willReturn(List.of(member));
        given(geminiClient.generateKeyPoints(any(), any(), any(), any()))
                .willReturn(List.of("새 키포인트"));
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.of(existingResume));
        given(resumeService.save(any(Resume.class))).willAnswer(inv -> inv.getArgument(0));

        resumeApiService.generate(EMAIL);

        verify(resumeService).save(same(existingResume));
        assertThat(existingResume.getExperiences()).hasSize(1);
        assertThat(existingResume.getExperiences().get(0).getKeyPoints()).containsExactly("새 키포인트");
    }

    @Test
    @DisplayName("generate()는 참가 프로젝트가 없으면 빈 experiences로 저장한다")
    void generate_emptyMemberships_savesResumeWithNoExperiences() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(USER_ID);

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(projectService.getMembershipsOf(USER_ID)).willReturn(List.of());
        given(resumeService.findByUserId(USER_ID)).willReturn(Optional.empty());
        given(resumeService.save(any(Resume.class))).willAnswer(inv -> inv.getArgument(0));

        resumeApiService.generate(EMAIL);

        ArgumentCaptor<Resume> captor = ArgumentCaptor.forClass(Resume.class);
        verify(resumeService).save(captor.capture());
        assertThat(captor.getValue().getExperiences()).isEmpty();
        verify(geminiClient, never()).generateKeyPoints(any(), any(), any(), any());
    }
}
