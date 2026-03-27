package kr.ac.kyonggi.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@TestConfiguration
@EnableJpaAuditing
@EntityScan("kr.ac.kyonggi.domain")
@EnableJpaRepositories("kr.ac.kyonggi.domain")
public class JpaTestConfig {

}
