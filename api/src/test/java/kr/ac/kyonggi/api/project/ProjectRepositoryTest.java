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
class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProjectRepository projectRepository;

    private User author;

    @BeforeEach
    void setUp() {
        author = em.persist(User.create(new UserCreateCommand("author@test.com", "pw", "작성자", null, null)));
        em.flush();
    }

    private Project saveProject(String title, String description, String category) {
        Project project = Project.create(new ProjectCreateCommand(
                title, description, category,
                List.of("Java", "Spring"),
                4,
                LocalDate.of(2026, 12, 31),
                author.getId()
        ));
        Project saved = em.persist(project);
        em.flush();
        return saved;
    }

    @Test
    @DisplayName("필터 없이 전체 프로젝트를 조회한다")
    void findWithFilters_noFilter_returnsAll() {
        saveProject("백엔드 프로젝트", "설명1", "백엔드");
        saveProject("프론트엔드 프로젝트", "설명2", "프론트엔드");

        Page<Project> result = projectRepository.findWithFilters(null, null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("category 필터로 해당 카테고리 프로젝트만 조회한다")
    void findWithFilters_categoryFilter_returnsMatching() {
        saveProject("백엔드 프로젝트", "설명", "백엔드");
        saveProject("프론트엔드 프로젝트", "설명", "프론트엔드");

        Page<Project> result = projectRepository.findWithFilters("백엔드", null, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("백엔드");
    }

    @Test
    @DisplayName("keyword가 title에 포함된 프로젝트를 조회한다")
    void findWithFilters_keywordFilter_matchesTitle() {
        saveProject("스프링 부트 프로젝트", "일반 설명", "백엔드");
        saveProject("리액트 프로젝트", "일반 설명", "프론트엔드");

        Page<Project> result = projectRepository.findWithFilters(null, "스프링", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).contains("스프링");
    }

    @Test
    @DisplayName("keyword가 description에 포함된 프로젝트를 조회한다")
    void findWithFilters_keywordFilter_matchesDescription() {
        saveProject("프로젝트A", "MSA 아키텍처 프로젝트입니다", "백엔드");
        saveProject("프로젝트B", "일반 설명", "백엔드");

        Page<Project> result = projectRepository.findWithFilters(null, "MSA", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getDescription()).contains("MSA");
    }

    @Test
    @DisplayName("keyword 불일치 시 빈 결과를 반환한다")
    void findWithFilters_keywordFilter_noMatch_returnsEmpty() {
        saveProject("백엔드 프로젝트", "Spring 설명", "백엔드");

        Page<Project> result = projectRepository.findWithFilters(null, "존재하지않는키워드xyz", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("category와 keyword를 동시에 적용해 조회한다")
    void findWithFilters_categoryAndKeyword_combined() {
        saveProject("백엔드 API 프로젝트", "REST API 개발", "백엔드");
        saveProject("백엔드 배치 프로젝트", "배치 처리", "백엔드");
        saveProject("프론트엔드 API 연동", "API 연동 프로젝트", "프론트엔드");

        Page<Project> result = projectRepository.findWithFilters("백엔드", "API", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).contains("API");
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("백엔드");
    }

    @Test
    @DisplayName("페이지네이션이 올바르게 동작한다")
    void findWithFilters_pagination_returnsCorrectPage() {
        for (int i = 1; i <= 5; i++) {
            saveProject("프로젝트" + i, "설명" + i, "백엔드");
        }

        Page<Project> firstPage = projectRepository.findWithFilters(null, null, PageRequest.of(0, 3));
        Page<Project> secondPage = projectRepository.findWithFilters(null, null, PageRequest.of(1, 3));

        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getContent()).hasSize(3);
        assertThat(secondPage.getContent()).hasSize(2);
    }
}
