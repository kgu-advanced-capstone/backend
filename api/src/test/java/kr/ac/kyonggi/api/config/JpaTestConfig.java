package kr.ac.kyonggi.api.config;

import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectRepositoryCustom;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@TestConfiguration
@EnableJpaAuditing
@EntityScan("kr.ac.kyonggi.domain")
@EnableJpaRepositories("kr.ac.kyonggi.domain")
public class JpaTestConfig {

    @Bean
    public ProjectRepositoryCustom projectRepositoryImpl() {
        return new ProjectRepositoryCustom() {
            @Override
            public Page<Project> findWithFilters(String category, String keyword, Pageable pageable) {
                return Page.empty(pageable);
            }

            @Override
            public List<Project> findContentWithFilters(String category, String keyword, Pageable pageable) {
                return List.of();
            }

            @Override
            public long countWithFilters(String category, String keyword) {
                return 0L;
            }
        };
    }
}
