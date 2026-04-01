package kr.ac.kyonggi.infrastructure.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileSystemStorageTest {

    private FileSystemStorage fileSystemStorage;
    private String rootDir;
    private final String baseUrl = "http://localhost:8080/files";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        rootDir = tempDir.toString();
        StorageProperties properties = new StorageProperties(rootDir, baseUrl);
        fileSystemStorage = new FileSystemStorage(properties);
    }

    @Test
    @DisplayName("upload() 성공 시 파일이 저장되고 URL을 반환한다")
    void upload_success() throws IOException {
        // given
        String originalFilename = "test.png";
        String content = "test content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        // when
        String fileUrl = fileSystemStorage.upload(inputStream, originalFilename, "image/png");

        // then
        assertThat(fileUrl).startsWith(baseUrl);
        String filename = fileUrl.substring(baseUrl.length() + 1);
        Path savedFile = tempDir.resolve(filename);
        assertThat(Files.exists(savedFile)).isTrue();
        assertThat(Files.readString(savedFile)).isEqualTo(content);
    }

    @Test
    @DisplayName("delete() 호출 시 저장된 파일이 삭제된다")
    void delete_success() throws IOException {
        // given
        String originalFilename = "test.png";
        InputStream inputStream = new ByteArrayInputStream("content".getBytes());
        String fileUrl = fileSystemStorage.upload(inputStream, originalFilename, "image/png");

        // when
        fileSystemStorage.delete(fileUrl);

        // then
        String filename = fileUrl.substring(baseUrl.length() + 1);
        Path savedFile = tempDir.resolve(filename);
        assertThat(Files.exists(savedFile)).isFalse();
    }

    @Test
    @DisplayName("delete() 호출 시 URL이 baseUrl로 시작하지 않으면 무시한다")
    void delete_invalidUrl() {
        // when & then (no exception should be thrown)
        fileSystemStorage.delete("http://other-domain.com/file.png");
    }
}
