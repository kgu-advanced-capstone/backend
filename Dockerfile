# 1. 빌드 결과물만 실행할 가벼운 Java 21 JRE 이미지 선택
# Alpine 리눅스 기반이라 용량이 매우 작습니다 (서버 디스크 절약)
FROM eclipse-temurin:21-jre-alpine

# 2. 컨테이너 내부 작업 디렉토리 설정
WORKDIR /app

# 3. JAR 파일 복사
# CI 단계(GitHub Actions)에서 빌드된 api-application 모듈의 jar 파일을 컨테이너로 가져옵니다.
# 파일명이 프로젝트 설정마다 다를 수 있으므로 와일드카드(*)를 사용합니다.
COPY api/build/libs/*.jar app.jar

# 4. 서버 자원(RAM) 최적화 실행 설정
# -Xmx400M: JVM이 최대 400MB만 사용하도록 제한 (1GB 램 서버에서 OS와 Redis를 보호하기 위함)
# -Dspring.profiles.active=prod: 운영 환경 프로필 적용
ENTRYPOINT ["java", \
            "-Xmx400M", \
            "-Dspring.profiles.active=prod", \
            "-Duser.timezone=Asia/Seoul", \
            "-jar", \
            "app.jar"]