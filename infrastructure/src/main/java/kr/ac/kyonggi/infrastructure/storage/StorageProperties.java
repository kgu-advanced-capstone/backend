package kr.ac.kyonggi.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 파일 저장 설정 프로퍼티.
 *
 * @param rootDir 로컬 저장소의 루트 디렉토리
 * @param baseUrl 파일 접근을 위한 베이스 URL
 */
@ConfigurationProperties(prefix = "storage.local")
public record StorageProperties(
        @DefaultValue("./uploads") String rootDir,
        @DefaultValue("http://localhost:8080/files") String baseUrl
) {
}
