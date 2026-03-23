package kr.ac.kyonggi.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "kr.ac.kyonggi")
@EntityScan(basePackages = "kr.ac.kyonggi.domain.entity")
@EnableJpaRepositories(basePackages = "kr.ac.kyonggi.domain.repository")
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}
}
