package kr.ac.kyonggi.api.project;

import kr.ac.kyonggi.api.config.QuerydslTestConfig;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectCreateCommand;
import kr.ac.kyonggi.domain.project.ProjectData;
import kr.ac.kyonggi.domain.project.ProjectRepository;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.project.ProjectServiceImpl;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslTestConfig.class, ProjectServiceImpl.class, ProjectCacheServiceTest.CacheTestConfig.class})
@ActiveProfiles("test")
class ProjectCacheServiceTest {

    @TestConfiguration
    @EnableCaching
    static class CacheTestConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("project");
        }
    }

    @Autowired
    private ProjectService projectService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestEntityManager em;

    private Long projectId;

    @BeforeEach
    void setUp() {
        User author = em.persist(User.create(new UserCreateCommand("author@test.com", "pw", "작성자", null)));
        em.flush();

        Project project = em.persist(Project.create(new ProjectCreateCommand(
                "캐시 테스트 프로젝트", "설명", "백엔드", List.of("Java"), 4,
                LocalDate.of(2026, 12, 31), author.getId()
        )));
        em.flush();
        projectId = project.getId();
    }

    @Test
    @DisplayName("getProjectData() 첫 호출 후 캐시에 저장된다")
    void getProjectData_firstCall_storesInCache() {
        projectService.getProjectData(projectId);

        Cache cache = cacheManager.getCache("project");
        assertThat(cache).isNotNull();
        assertThat(cache.get(projectId, ProjectData.class)).isNotNull();
        assertThat(cache.get(projectId, ProjectData.class).title()).isEqualTo("캐시 테스트 프로젝트");
    }

    @Test
    @DisplayName("getProjectData() 두 번째 호출은 캐시에서 반환한다")
    void getProjectData_secondCall_returnsFromCache() {
        projectService.getProjectData(projectId);

        projectRepository.deleteById(projectId);
        em.flush();

        ProjectData cached = projectService.getProjectData(projectId);
        assertThat(cached.title()).isEqualTo("캐시 테스트 프로젝트");
    }

    @Test
    @DisplayName("apply() 호출 시 해당 프로젝트 캐시가 evict된다")
    void apply_evictsCache() {
        projectService.getProjectData(projectId);
        assertThat(cacheManager.getCache("project").get(projectId)).isNotNull();

        projectService.apply(projectId, 999L);

        assertThat(cacheManager.getCache("project").get(projectId)).isNull();
    }

    @Test
    @DisplayName("updateStatus() 호출 시 해당 프로젝트 캐시가 evict된다")
    void updateStatus_evictsCache() {
        projectService.getProjectData(projectId);
        assertThat(cacheManager.getCache("project").get(projectId)).isNotNull();

        User author = em.persist(User.create(new UserCreateCommand("author2@test.com", "pw", "작성자2", null)));
        em.flush();
        Project p = projectRepository.findById(projectId).orElseThrow();

        projectService.updateStatus(projectId, p.getAuthorId(), kr.ac.kyonggi.domain.project.ProjectStatus.IN_PROGRESS);

        assertThat(cacheManager.getCache("project").get(projectId)).isNull();
    }
}
