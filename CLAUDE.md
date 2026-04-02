# CLAUDE.md
작업 절차와 프로젝트 설명 포함.
코드 작성 절차 **반드시 준수**.
반드시 아래에 작성된 코드 작성 절차를 그대로 말하고 최초 시작.

# 코드 작성 절차 (반드시 준수)

```
1. 요구 사항 분석 - 요구 사항 및 현재 코드 상세 분석.
2. 브랜치 분리 - 필요한 경우 origin/develop에서 브랜치 생성 (hotfix는 main)
3. 테스트 작성 — 구현 전 실패하는 테스트 먼저 작성.
4. 테스트 실패 확인 — ./gradlew test 로 실패 확인.
5. 구현 — 테스트 통과를 위한 최소한의 코드 작성. 완료 후 린터 실행.
6. 테스트 성공 확인 — ./gradlew test 로 전체 통과 확인.
7. 커밋 & 푸시 — 통과 후 커밋 & 푸시.
8. PR 생성 — origin/develop 대상으로 PR 생성.
```

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

# Start local dev dependencies (MySQL + Redis)
docker-compose up -d
```

Local services after startup:
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

## Architecture

Multi-module Gradle project (Java 21, Spring Boot 3.5.11). The `api` module is the only executable; the others are library JARs.
Module dependency direction: `api` → `domain`, `infrastructure`, `common`. Lower modules do not depend on each other.

### Module Roles

| Module | Layer | Responsibility | Contains |
|---|---|---|---|
| `api` | Presentation | 외부 요청 처리 및 실행 | Controller, DTO (request/response), Security, Swagger, Config |
| `domain` | Business | 핵심 비즈니스 로직 | Entity, Service (interface + impl), Repository interface, Domain Event |
| `infrastructure` | Implementation | 기술적 세부 구현 (외부 연동) | JPA/Querydsl 구현체, Redis, Kafka, 외부 API, Mail, Config |
| `common` | Shared | 공용 유틸리티 | Exception (BusinessException, GlobalExceptionHandler), response 규격, util, constants |

### Package Structure

```
kr.ac.kyonggi.api
├── [API명]/          ← API 인터페이스 및 구현체
└── config/           ← SecurityConfig, SwaggerConfig 등

kr.ac.kyonggi.domain
└── [도메인명]/        ← 도메인 로직

kr.ac.kyonggi.infrastructure
├── persistence/
├── external/         ← 외부 API 클라이언트
├── messaging/        ← Kafka, Redis Listener
└── 기타 등등...

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

- New JPA entities → `domain/entity/`
- Repository interfaces (Spring Data JPA) → `domain/repository/`
- Domain business logic → `domain/service/`
- REST controllers → `api/controller/`
- Request/Response DTOs → `api/dto/request/` and `api/dto/response/` (Java record 사용)
- Facade services (DTO 변환, 여러 domain/infra 조합) → `api/service/`
- Custom exceptions → `common/exception/` (BusinessException 상속)
- Querydsl 구현체, Redis/Kafka 설정 → `infrastructure/`

## DB 스키마 관리

```
db/
├── ddl.sql              ← 베이스 스키마 (전체 테이블 DROP & CREATE)
├── new-migration.sh     ← 마이그레이션 파일 생성 스크립트
└── migrations/          ← 증분 변경 SQL 모음
    └── V{YYYYMMDDHHmmss}.sql
```

### 규칙

- `db/ddl.sql` 직접 수정 금지. 스키마 변경은 마이그레이션 파일로 작성.
- 엔티티(`@Column`, `@Table` 등) 변경 시 대응하는 마이그레이션 파일 함께 추가.

### 마이그레이션 파일 생성

```bash
bash db/new-migration.sh
```

실행 시 새 마이그레이션 파일 생성됨. 변경 내용은 파일명이 아닌 파일 내부 주석으로 작성.

### 네이밍 규칙

`V{YYYYMMDDHHmmss}.sql`

| 부분 | 설명 | 예시 |
|---|---|---|
| `V` | 버전 prefix | 고정 |
| `YYYYMMDDHHmmss` | 생성 일시 (초 단위) | `20260331143000` |

예시: `V20260331143000.sql`

## Language

- 모든 커밋 메시지, PR 제목/본문, 코드 주석은 **한국어**로 작성.
- CLAUDE.md, 설정 파일 등 프로젝트 문서도 한국어 기본.

## Git Convention

### Branch Strategy (Git Flow)

- `main` — 프로덕션 배포 브랜치. 직접 커밋 금지.
- `develop` — 개발 통합 브랜치. 모든 작업 브랜치는 여기서 분기하고 여기로 PR.
- 작업 브랜치는 반드시 `develop`에서 생성.

| Prefix | 용도 | 예시 |
|---|---|---|
| `feat/` 또는 `feature/` | 새 기능 개발 | `feat/logout` |
| `fix/` | 버그 수정 | `fix/session-expiry` |
| `chore/` | CI, 설정, 문서 등 비기능 작업 | `chore/pr-test` |
| `refactor/` | 리팩토링 | `refactor/auth-service` |

### 작업 흐름

```
1. develop 브랜치에서 새 브랜치 생성
   git checkout develop && git pull origin develop
   git checkout -b feat/기능명

2. 작업 후 커밋 & 푸시
   git add <파일> && git commit -m "feat: 설명"
   git push origin feat/기능명

3. GitHub에서 develop 대상으로 PR 생성

4. 리뷰 & 머지 후, 릴리스 시점에 develop → main 머지
```

**주의사항:**
- 현재 브랜치와 관계없는 변경 커밋 금지. 별도 작업은 develop에서 새 브랜치 생성 후 진행.
- 하나의 브랜치에는 하나의 주제만.

### Commit Message

Conventional Commits 형식:

```
<type>: <한국어 설명>
```

| Type | 용도 |
|---|---|
| `feat` | 새 기능 추가 |
| `fix` | 버그 수정 |
| `chore` | CI, 설정, 빌드 등 비기능 변경 |
| `refactor` | 리팩토링 (기능 변경 없음) |
| `test` | 테스트 추가/수정 |
| `docs` | 문서 변경 |

- 커밋 메시지 type은 브랜치 prefix와 일치 (예: `feat/` 브랜치 → `feat:` 커밋).
- `Co-Authored-By` 트레일러 추가 금지.

### PR

- 모든 PR의 base 브랜치는 `origin/develop` (`main` 아님).
- `develop` → `main` 머지는 릴리스 시점에만.
