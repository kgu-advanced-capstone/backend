package kr.ac.kyonggi.api.config;


import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("캡스톤 백엔드")
                        .description("스웨거 API 명세.")
                        .version("v1.0.0"));
    }

}
