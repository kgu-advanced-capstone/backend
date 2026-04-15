package kr.ac.kyonggi.api.project;

import kr.ac.kyonggi.api.config.JpaTestConfig;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectCreateCommand;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectMemberCreateCommand;
import kr.ac.kyonggi.domain.project.ProjectMemberRepository;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
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

@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
class ProjectMemberRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProjectMemberRepository projectMemberRepository;

    private User user1;
    private User user2;
    private Project project;

    @BeforeEach
    void setUp() {
        user1 = em.persist(User.create(new UserCreateCommand("user1@test.com", "pw", "유저1", null, null)));
        user2 = em.persist(User.create(new UserCreateCommand("user2@test.com", "pw", "유저2", null, null)));
        project = em.persist(Project.create(new ProjectCreateCommand(
                "테스트 프로젝트", "설명", "백엔드",
                List.of("Java"), 4, LocalDate.of(2026, 12, 31), user1.getId()
        )));
        em.flush();
    }

    private ProjectMember addMember(Project p, User u) {
        ProjectMember member = ProjectMember.of(new ProjectMemberCreateCommand(p.getId(), u.getId()));
        ProjectMember saved = em.persist(member);
        em.flush();
        return saved;
    }

    @Test
    @DisplayName("멤버가 존재하면 existsByProjectIdAndUserId가 true를 반환한다")
    void existsByProjectIdAndUserId_exists_returnsTrue() {
        addMember(project, user1);

        boolean exists = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user1.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("멤버가 없으면 existsByProjectIdAndUserId가 false를 반환한다")
    void existsByProjectIdAndUserId_notExists_returnsFalse() {
        boolean exists = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user2.getId());

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("countByProjectId는 프로젝트에 참가한 멤버 수를 반환한다")
    void countByProjectId_returnsCorrectCount() {
        addMember(project, user1);
        addMember(project, user2);

        long count = projectMemberRepository.countByProjectId(project.getId());

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("findByUserId는 해당 유저가 참가한 멤버 목록을 반환한다")
    void findByUserId_returnsUserMemberships() {
        Project project2 = em.persist(Project.create(new ProjectCreateCommand(
                "두 번째 프로젝트", "설명", "프론트엔드",
                List.of("React"), 3, LocalDate.of(2026, 12, 31), user2.getId()
        )));
        em.flush();

        addMember(project, user1);
        addMember(project2, user1);

        List<ProjectMember> memberships = projectMemberRepository.findByUserId(user1.getId());

        assertThat(memberships).hasSize(2);
        assertThat(memberships).extracting(ProjectMember::getUserId).containsOnly(user1.getId());
    }

    @Test
    @DisplayName("findByProjectId는 해당 프로젝트의 멤버 목록을 반환한다")
    void findByProjectId_returnsProjectMembers() {
        addMember(project, user1);
        addMember(project, user2);

        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());

        assertThat(members).hasSize(2);
        assertThat(members).extracting(ProjectMember::getProjectId).containsOnly(project.getId());
    }
}
