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
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupabaseS3Storage implements FileStorage {

    private final S3Client s3Client;
    private final StorageProperties properties;

    @Override
    public String upload(InputStream inputStream, String originalFilename, String contentType) {
        String filename = generateUniqueFilename(originalFilename);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(filename)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(inputStream, inputStream.available()));

            return String.format("%s/%s/%s", properties.publicFileUrl(), properties.bucket(), filename);

        } catch (IOException e) {
            log.error("S3 파일 저장 실패: {}", originalFilename, e);
            throw new StorageException("S3 파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null) {
            return;
        }

        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(filename)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
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
