package kr.ac.kyonggi.domain.experience;

import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectCreateCommand;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExperienceTest {

    private User user;
    private Project project;

    @BeforeEach
    void setUp() {
        user = User.create(new UserCreateCommand("test@test.com", "pw", "홍길동", null));
        ReflectionTestUtils.setField(user, "id", 1L);

        project = Project.create(new ProjectCreateCommand(
                "테스트 프로젝트", "설명", "백엔드", List.of("Java"), 4,
                LocalDate.of(2026, 12, 31), user
        ));
        ReflectionTestUtils.setField(project, "id", 10L);
    }

    @Test
    @DisplayName("create() 직후 content, user, project가 올바르게 설정된다")
    void create_setsFieldsCorrectly() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(user, project, "### 구현 내용\n로그인 기능을 개발했습니다."));

        assertThat(experience.getContent()).isEqualTo("### 구현 내용\n로그인 기능을 개발했습니다.");
        assertThat(experience.getUser()).isSameAs(user);
        assertThat(experience.getProject()).isSameAs(project);
    }

    @Test
    @DisplayName("create() 직후 aiSummary는 null이다")
    void create_initialAiSummaryIsNull() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(user, project, "내용"));

        assertThat(experience.getAiSummary()).isNull();
    }

    @Test
    @DisplayName("updateContent()를 호출하면 content가 변경된다")
    void updateContent_changesContent() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(user, project, "기존 내용"));

        experience.updateContent("새로운 내용");

        assertThat(experience.getContent()).isEqualTo("새로운 내용");
    }

    @Test
    @DisplayName("updateAiSummary()를 호출하면 aiSummary가 설정된다")
    void updateAiSummary_setsAiSummary() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(user, project, "내용"));

        experience.updateAiSummary("JWT를 활용한 보안 인증 시스템 구축");

        assertThat(experience.getAiSummary()).isEqualTo("JWT를 활용한 보안 인증 시스템 구축");
    }
}
