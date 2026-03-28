package kr.ac.kyonggi.domain.project;

import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTest {

    private User author;

    @BeforeEach
    void setUp() {
        author = User.create(new UserCreateCommand("author@test.com", "pw", "작성자", null));
        ReflectionTestUtils.setField(author, "id", 1L);
    }

    private ProjectCreateCommand defaultCommand() {
        return new ProjectCreateCommand(
                "테스트 프로젝트",
                "프로젝트 설명",
                "백엔드",
                List.of("Java", "Spring"),
                4,
                LocalDate.of(2026, 12, 31),
                author
        );
    }

    @Test
    @DisplayName("프로젝트 생성 직후 초기 상태는 RECRUITING이다.")
    void create_initialStatusIsRecruiting() {
        Project project = Project.create(defaultCommand());

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.RECRUITING);
    }

    @Test
    @DisplayName("프로젝트 상태를 IN_PROGRESS로 변경할 수 있다.")
    void updateStatus_toInProgress() {
        Project project = Project.create(defaultCommand());

        project.updateStatus(ProjectStatus.IN_PROGRESS);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("상태를 COMPLETED로 변경할 수 있다.")
    void updateStatus_toCompleted() {
        Project project = Project.create(defaultCommand());

        project.updateStatus(ProjectStatus.COMPLETED);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
    }

    @Test
    @DisplayName("isAuthor()는 작성자 ID와 일치하면 true를 반환한다")
    void isAuthor_withMatchingId_returnsTrue() {
        Project project = Project.create(defaultCommand());

        assertThat(project.isAuthor(1L)).isTrue();
    }

    @Test
    @DisplayName("isAuthor()는 작성자 ID와 다르면 false를 반환한다")
    void isAuthor_withDifferentId_returnsFalse() {
        Project project = Project.create(defaultCommand());

        assertThat(project.isAuthor(99L)).isFalse();
    }
}
