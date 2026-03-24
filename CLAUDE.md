# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build the API module (produces deployable JAR)
./gradlew clean :api:bootJar

# Build all modules
./gradlew clean build

# Run tests (all modules)
./gradlew test

# Run tests for a single module
./gradlew :api:test

# Run locally (requires MySQL + Redis running)
./gradlew :api:bootRun
# or with explicit profile:
./gradlew :api:bootRun --args='--spring.profiles.active=local'

# Start local dev dependencies (MySQL + Redis)
docker-compose up -d
```

Local services after startup:
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`
- Prometheus metrics: `http://localhost:8080/actuator/prometheus`

## Architecture

Multi-module Gradle project (Java 21, Spring Boot 3.5.11). The `api` module is the only executable; the others are library JARs.

Module dependency direction: `api` → `domain`, `infrastructure`, `common`. Lower modules do not depend on each other.

### Module Roles

| Module | Layer | Responsibility | Contains |
|---|---|---|---|
| `api` | Presentation | 외부 요청 처리 및 실행 | Controller, DTO (request/response), Security, Swagger, Config |
| `domain` | Business | 핵심 비즈니스 로직 | Entity, Service (interface + impl), Repository interface, Domain Event |
| `infrastructure` | Implementation | 기술적 세부 구현 (외부 연동) | JPA/Querydsl 구현체, Redis, Kafka, 외부 API, Mail, Config |
| `common` | Shared | 전역 공용 유틸리티 | Exception (BusinessException, GlobalExceptionHandler), response 규격, util, constants |

### Package Structure

```
kr.ac.kyonggi.api
├── controller/
├── service/          ← Application Service / Facade (domain+infra 조합)
├── dto/
│   ├── request/
│   └── response/
├── security/         ← CustomUserDetailsService 등
└── config/           ← SecurityConfig, SwaggerConfig 등

kr.ac.kyonggi.domain
├── entity/
├── service/          ← Domain Service (순수 비즈니스 로직, DTO 미사용)
├── repository/       ← Spring Data JPA interface
└── exception/        ← 도메인 전용 예외 (선택)

kr.ac.kyonggi.infrastructure
├── persistence/      ← Querydsl 구현체
├── external/         ← 외부 API 클라이언트
├── messaging/        ← Kafka, Redis Listener
└── config/

kr.ac.kyonggi.common
├── exception/        ← BusinessException, GlobalExceptionHandler
├── response/         ← ApiResponse, ErrorResponse
├── util/
└── constants/
```

**Key technology decisions:**
- **MySQL 8.0** as primary store; **Redis** as cache layer
- **Spring Security 6** with session-based auth (no JWT); `SecurityConfig` in `api/config/`
- **Prometheus/Micrometer** metrics tagged with `application: api`; HikariCP pool named `mysql-main-pool`
- **Spring profiles:** `local` (docker-compose credentials, DDL auto-update) and `prod` (injected via `.env` on Azure VM)
- **`@SpringBootApplication`** must use `scanBasePackages = "kr.ac.kyonggi"` with `@EnableJpaRepositories` and `@EntityScan` pointing to `kr.ac.kyonggi.domain` to discover beans across modules
- **DTOs use Java records** — no Lombok needed; `@Valid` Bean Validation annotations go directly on record components; accessors are `field()` style (e.g., `response.email()`, not `response.getEmail()`)

## CI/CD

GitHub Actions (`.github/workflows/deploy.yml`):
- Builds on push to `main`, `feat/**`, `feature/**`
- Deploys Docker image to Azure VM only on `main` branch
- Deployment requires secrets: `DOCKER_USERNAME`, `DOCKER_PASSWORD`, `SERVER_HOST`, `SSH_USERNAME`, `SSH_PRIVATE_KEY`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `GCLOUD_RW_API_KEY`

Docker image uses `eclipse-temurin:21-jre-alpine` with `-Xmx400M` and `spring.profiles.active=prod`.

## Placement Rules

When adding new code, follow these rules:
- New JPA entities → `domain/entity/`
- Repository interfaces (Spring Data JPA) → `domain/repository/`
- Domain business logic → `domain/service/`
- REST controllers → `api/controller/`
- Request/Response DTOs → `api/dto/request/` and `api/dto/response/` (Java record 사용)
- Facade services (DTO 변환, 여러 domain/infra 조합) → `api/service/`
- Custom exceptions → `common/exception/` (BusinessException 상속)
- Querydsl 구현체, Redis/Kafka 설정 → `infrastructure/`
