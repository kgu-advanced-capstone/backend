package kr.ac.kyonggi.domain.storage;

import java.io.InputStream;

/**
 * 파일 저장 추상화 인터페이스.
 * 로컬 파일 시스템, S3 등으로 교체 가능하도록 설계됨.
 * 도메인 계층에 위치하여 의존성 역전을 실현함.
 */
public interface FileStorage {

    /**
     * 파일을 업로드하고 저장된 경로(또는 URL)를 반환한다.
     *
     * @param inputStream      파일 데이터 스트림
     * @param originalFilename 원본 파일 이름
     * @param contentType      콘텐츠 타입 (MIME type)
     * @return 저장된 파일의 경로 또는 접근 가능한 URL
     */
    String upload(InputStream inputStream, String originalFilename, String contentType);

    /**
     * 저장된 파일을 삭제한다.
     *
     * @param fileUrl 삭제할 파일의 경로 또는 URL
     */
    void delete(String fileUrl);
}
