package kr.ac.kyonggi.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@EntityScan(basePackages = "kr.ac.kyonggi.domain")
@EnableJpaRepositories(basePackages = "kr.ac.kyonggi.domain")
public class JpaConfig {
}
