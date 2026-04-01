package kr.ac.kyonggi.infrastructure.storage;

import kr.ac.kyonggi.common.exception.StorageException;
import kr.ac.kyonggi.domain.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 로컬 서버 파일 시스템을 사용하는 파일 저장소 구현체.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileSystemStorage implements FileStorage {

    private final StorageProperties properties;

    @Override
    public String upload(InputStream inputStream, String originalFilename, String contentType) {
        String filename = generateUniqueFilename(originalFilename);
        Path targetLocation = getTargetLocation(filename);

        try {
            ensureDirectoryExists(targetLocation.getParent());
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 저장 완료: {}", targetLocation);
            return properties.baseUrl() + "/" + filename;
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", originalFilename, e);
            throw new StorageException("파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.startsWith(properties.baseUrl())) {
            log.warn("삭제할 수 없는 파일 URL입니다: {}", fileUrl);
            return;
        }

        String filename = fileUrl.substring(properties.baseUrl().length() + 1);
        Path targetLocation = getTargetLocation(filename);

        try {
            Files.deleteIfExists(targetLocation);
            log.info("파일 삭제 완료: {}", targetLocation);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", targetLocation, e);
            throw new StorageException("파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }

    private Path getTargetLocation(String filename) {
        return Paths.get(properties.rootDir()).resolve(filename).normalize();
    }

    private void ensureDirectoryExists(Path directory) throws IOException {
        if (Files.notExists(directory)) {
            Files.createDirectories(directory);
        }
    }
}
