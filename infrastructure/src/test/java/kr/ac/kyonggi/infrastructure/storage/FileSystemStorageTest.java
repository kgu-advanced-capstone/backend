package kr.ac.kyonggi.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Mockito 사용 설정
class FileSystemStorageTest {

    private SupabaseS3Storage fileSystemStorage;

    @Mock
    private S3Client s3Client; // 가짜 S3 클라이언트

    private final String bucket = "capstone";
    private final String endpoint = "https://supabase.co/storage/v1/s3";
    private final String publicFileUrl = "https://test.com";

    @BeforeEach
    void setUp() {
        // 실제 yml을 읽지 않고 테스트용 설정값을 직접 주입
        StorageProperties properties = new StorageProperties(bucket, endpoint, publicFileUrl);
        fileSystemStorage = new SupabaseS3Storage(s3Client, properties);
    }

    @Test
    @DisplayName("S3 업로드 시 UUID가 포함된 URL을 반환해야 한다 (Mock)")
    void upload_success_mock() {
        // given: s3Client.putObject가 호출되면 성공 응답을 보내준다고 가정
        when(s3Client.putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        String originalFilename = "test.png";
        InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

        // when
        String fileUrl = fileSystemStorage.upload(inputStream, originalFilename, "image/png");

        // then
        // 1. 반환된 URL이 설정한 버킷과 주소를 포함하는지 확인
        assertThat(fileUrl).contains(bucket);
        assertThat(fileUrl).contains(publicFileUrl);

        // 2. 실제로 S3Client의 putObject 메서드가 실행되었는지 확인
        verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));

        System.out.println("✅ Mock 업로드 테스트 완료: " + fileUrl);
    }

    @Test
    @DisplayName("S3 삭제 호출 시 s3Client의 deleteObject가 실행되어야 한다 (Mock)")
    void delete_success_mock() {
        // given: deleteObject가 성공한다고 가정
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        String fileUrl = endpoint + "/" + publicFileUrl + "/test-uuid.png";

        // when
        fileSystemStorage.delete(fileUrl);

        // then
        // 3. 실제로 S3Client의 deleteObject 메서드가 1번 호출되었는지 검증
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));

        System.out.println("✅ Mock 삭제 테스트 완료");
    }
}