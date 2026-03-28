package kr.ac.kyonggi.api.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.ac.kyonggi.domain.project.ProjectRepositoryCustom;
import kr.ac.kyonggi.infrastructure.persistence.ProjectRepositoryImpl;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaAuditing
@EntityScan("kr.ac.kyonggi.domain")
@EnableJpaRepositories("kr.ac.kyonggi.domain")
public class QuerydslTestConfig {

    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(em);
    }

    @Bean
    public ProjectRepositoryCustom projectRepositoryImpl(JPAQueryFactory jpaQueryFactory) {
        return new ProjectRepositoryImpl(jpaQueryFactory);
    }
}
