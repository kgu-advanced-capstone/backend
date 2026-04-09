package kr.ac.kyonggi.api.project;

import kr.ac.kyonggi.domain.project.ProjectMemberRepository;
import kr.ac.kyonggi.domain.project.ProjectRepository;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.project.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ProjectCountCacheTest.CacheTestConfig.class)
class ProjectCountCacheTest {

    @EnableCaching
    @Configuration
    static class CacheTestConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("project-count");
        }

        @Bean
        public ProjectRepository projectRepository() {
            return mock(ProjectRepository.class);
        }

        @Bean
        public ProjectMemberRepository projectMemberRepository() {
            return mock(ProjectMemberRepository.class);
        }

        @Bean
        public ProjectService projectService(ProjectRepository projectRepository,
                                             ProjectMemberRepository projectMemberRepository) {
            return new ProjectServiceImpl(projectRepository, projectMemberRepository);
        }
    }

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> {
            var cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });
        reset(projectRepository);
    }

    @Test
    @DisplayName("countProjects 첫 호출 시 DB를 조회한다")
    void countProjects_firstCall_queriesDb() {
        when(projectRepository.countWithFilters(null, null)).thenReturn(100L);

        long result = projectService.countProjects(null, null);

        assertThat(result).isEqualTo(100L);
        verify(projectRepository, times(1)).countWithFilters(null, null);
    }

    @Test
    @DisplayName("countProjects 두 번째 호출은 캐시에서 반환되어 DB 조회가 발생하지 않는다")
    void countProjects_secondCall_returnsFromCache() {
        when(projectRepository.countWithFilters(null, null)).thenReturn(100L);

        projectService.countProjects(null, null);
        long result = projectService.countProjects(null, null);

        assertThat(result).isEqualTo(100L);
        verify(projectRepository, times(1)).countWithFilters(null, null);
    }

    @Test
    @DisplayName("카테고리별 count는 카테고리마다 별도 캐시 키로 저장된다")
    void countProjects_differentCategories_cachedSeparately() {
        when(projectRepository.countWithFilters("백엔드", null)).thenReturn(50L);
        when(projectRepository.countWithFilters("프론트엔드", null)).thenReturn(70L);

        projectService.countProjects("백엔드", null);
        projectService.countProjects("프론트엔드", null);
        long backend = projectService.countProjects("백엔드", null);
        long frontend = projectService.countProjects("프론트엔드", null);

        assertThat(backend).isEqualTo(50L);
        assertThat(frontend).isEqualTo(70L);
        verify(projectRepository, times(1)).countWithFilters("백엔드", null);
        verify(projectRepository, times(1)).countWithFilters("프론트엔드", null);
    }

    @Test
    @DisplayName("keyword가 있을 때는 캐싱되지 않아 매 호출마다 DB를 조회한다")
    void countProjects_withKeyword_notCached() {
        when(projectRepository.countWithFilters(null, "스프링")).thenReturn(30L);

        projectService.countProjects(null, "스프링");
        projectService.countProjects(null, "스프링");

        verify(projectRepository, times(2)).countWithFilters(null, "스프링");
    }
}
