package kr.ac.kyonggi.domain.project;

import kr.ac.kyonggi.common.exception.ProjectFullException;
import kr.ac.kyonggi.common.exception.ProjectNotRecruitingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectTest {

    private ProjectCreateCommand defaultCommand() {
        return new ProjectCreateCommand(
                "테스트 프로젝트",
                "프로젝트 설명",
                "백엔드",
                List.of("Java", "Spring"),
                4,
                LocalDate.of(2026, 12, 31),
                1L
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

    // ── addMember() ───────────────────────────────────────────────────

    @Test
    @DisplayName("addMember()는 RECRUITING 상태에서 currentMemberCount를 1 증가시킨다")
    void addMember_whenRecruiting_incrementsCount() {
        Project project = Project.create(defaultCommand());

        project.addMember();

        assertThat(project.getCurrentMemberCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("addMember()는 RECRUITING이 아닌 상태에서 ProjectNotRecruitingException을 던진다")
    void addMember_whenNotRecruiting_throwsProjectNotRecruitingException() {
        Project project = Project.create(defaultCommand());
        project.updateStatus(ProjectStatus.IN_PROGRESS);

        assertThatThrownBy(project::addMember)
                .isInstanceOf(ProjectNotRecruitingException.class);
    }

    @Test
    @DisplayName("addMember()는 currentMemberCount가 maxMembers에 도달한 경우 ProjectFullException을 던진다")
    void addMember_whenFull_throwsProjectFullException() {
        Project project = Project.create(defaultCommand()); // maxMembers = 4
        ReflectionTestUtils.setField(project, "currentMemberCount", 4);

        assertThatThrownBy(project::addMember)
                .isInstanceOf(ProjectFullException.class);
    }
}
