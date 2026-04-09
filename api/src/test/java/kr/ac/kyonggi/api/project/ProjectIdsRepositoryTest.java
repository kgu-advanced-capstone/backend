package kr.ac.kyonggi.api.project;

import kr.ac.kyonggi.api.config.QuerydslTestConfig;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectCreateCommand;
import kr.ac.kyonggi.domain.project.ProjectRepository;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslTestConfig.class)
@ActiveProfiles("test")
class ProjectIdsRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProjectRepository projectRepository;

    private Long authorId;

    @BeforeEach
    void setUp() {
        User author = em.persist(User.create(new UserCreateCommand("author@test.com", "pw", "작성자", null)));
        em.flush();
        authorId = author.getId();
    }

    private Project saveProject(String title, String category) {
        Project project = Project.create(new ProjectCreateCommand(
                title, "설명", category, List.of("Java"), 4,
                LocalDate.of(2026, 12, 31), authorId
        ));
        Project saved = em.persist(project);
        em.flush();
        return saved;
    }

    @Test
    @DisplayName("findIdsByFilters()는 프로젝트 ID만 반환한다")
    void findIdsByFilters_returnsOnlyIds() {
        Project p1 = saveProject("프로젝트A", "백엔드");
        Project p2 = saveProject("프로젝트B", "백엔드");

        Page<Long> result = projectRepository.findIdsByFilters(null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).containsExactlyInAnyOrder(p1.getId(), p2.getId());
    }

    @Test
    @DisplayName("findIdsByFilters()는 category 필터를 적용한다")
    void findIdsByFilters_categoryFilter() {
        Project p1 = saveProject("백엔드 프로젝트", "백엔드");
        saveProject("프론트 프로젝트", "프론트엔드");

        Page<Long> result = projectRepository.findIdsByFilters("백엔드", null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(p1.getId());
    }

    @Test
    @DisplayName("findIdsByFilters()는 keyword 필터를 적용한다")
    void findIdsByFilters_keywordFilter() {
        Project p1 = saveProject("스프링 부트 프로젝트", "백엔드");
        saveProject("리액트 프로젝트", "프론트엔드");

        Page<Long> result = projectRepository.findIdsByFilters(null, "스프링", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(p1.getId());
    }

    @Test
    @DisplayName("findIdsByFilters()는 페이지네이션을 적용한다")
    void findIdsByFilters_pagination() {
        for (int i = 1; i <= 5; i++) {
            saveProject("프로젝트" + i, "백엔드");
        }

        Page<Long> page1 = projectRepository.findIdsByFilters(null, null, PageRequest.of(0, 3));
        Page<Long> page2 = projectRepository.findIdsByFilters(null, null, PageRequest.of(1, 3));

        assertThat(page1.getTotalElements()).isEqualTo(5);
        assertThat(page1.getContent()).hasSize(3);
        assertThat(page2.getContent()).hasSize(2);
    }
}
