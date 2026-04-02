package kr.ac.kyonggi.infrastructure.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 파일 저장 설정 프로퍼티.
 *
 */
@ConfigurationProperties(prefix = "storage.s3")
public record StorageProperties(
        String bucket,
        String endpoint
) {
}
