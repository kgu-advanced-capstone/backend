package kr.ac.kyonggi.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EntityScan("kr.ac.kyonggi.domain.entity")
@EnableJpaRepositories("kr.ac.kyonggi.domain.repository")
public class JpaTestConfig {

}
