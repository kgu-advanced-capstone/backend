package kr.ac.kyonggi.api.project;

import kr.ac.kyonggi.api.project.dto.ParticipantResponse;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectMemberCreateCommand;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ParticipantResponseTest {

    @Test
    @DisplayName("ParticipantResponse.of()는 User의 email과 phone을 포함해야 한다")
    void of_includesEmailAndPhone() {
        User user = User.create(new UserCreateCommand("test@example.com", "pw", "홍길동", null, null));
        ReflectionTestUtils.setField(user, "phone", "010-1234-5678");

        ProjectMember member = ProjectMember.of(new ProjectMemberCreateCommand(1L, 1L));

        ParticipantResponse response = ParticipantResponse.of(user, member);

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.phone()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("ParticipantResponse.of()는 phone이 null이어도 정상 동작한다")
    void of_phoneNullIsAllowed() {
        User user = User.create(new UserCreateCommand("test@example.com", "pw", "홍길동", null, null));

        ProjectMember member = ProjectMember.of(new ProjectMemberCreateCommand(1L, 1L));

        ParticipantResponse response = ParticipantResponse.of(user, member);

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.phone()).isNull();
    }
}
