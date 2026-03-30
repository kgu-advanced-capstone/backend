package kr.ac.kyonggi.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme cookieAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.COOKIE)
                .name("JSESSIONID")
                .description("세션 쿠키 기반 인증. /api/auth/login으로 로그인 후 자동 설정됩니다.");

        return new OpenAPI()
                .info(new Info()
                        .title("캡스톤 백엔드")
                        .description("스웨거 API 명세.")
                        .version("v1.0.0"))
                .components(new Components().addSecuritySchemes("cookieAuth", cookieAuth))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"));
    }

}
