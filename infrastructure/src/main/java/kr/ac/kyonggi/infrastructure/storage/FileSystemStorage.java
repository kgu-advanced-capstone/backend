package kr.ac.kyonggi.infrastructure.storage;

import kr.ac.kyonggi.common.exception.StorageException;
import kr.ac.kyonggi.domain.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

    private final S3Client s3Client;
    private final StorageProperties properties;

    @Override
    public String upload(InputStream inputStream, String originalFilename, String contentType) {
        // 1. 파일명 생성 (기존 로직과 동일하게 UUID 활용)
        String filename = generateUniqueFilename(originalFilename);

        try {
            // 2. S3 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(filename)
                    .contentType(contentType)
                    .build();

            // 3. S3로 파일 전송 (Files.copy 대신 s3Client 사용)
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(inputStream, (long) inputStream.available()));

            log.info("S3 파일 업로드 완료: {}", filename);

            // 4. 리턴값: DB에 저장될 최종 URL (기존baseUrl 역할)
            // Supabase S3 경로 형식: {endpoint}/{bucket}/{filename}
            return String.format("%s/%s/%s", properties.endpoint(), properties.bucket(), filename);

        } catch (IOException e) {
            log.error("S3 파일 저장 실패: {}", originalFilename, e);
            throw new StorageException("S3 파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        // 기존 baseUrl 검증 로직과 동일하게 유지 (endpoint와 bucket 경로 포함 여부 확인)
        if (fileUrl == null || !fileUrl.contains(properties.bucket())) {
            log.warn("삭제할 수 없는 S3 파일 URL입니다: {}", fileUrl);
            return;
        }

        // URL에서 파일명(Key)만 추출
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(filename)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 완료: {}", filename);
        } catch (Exception e) {
            log.error("S3 파일 삭제 실패: {}", fileUrl, e);
            throw new StorageException("S3 파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }


}
