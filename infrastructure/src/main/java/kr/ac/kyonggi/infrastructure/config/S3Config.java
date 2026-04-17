package kr.ac.kyonggi.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class    S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    @Bean
    public S3Client s3Client() {// [로그 추가] 어떤 값이 들어오는지 눈으로 확인해봅시다.

        try {
            System.out.println("DEBUG: S3 Endpoint -> " + endpoint);
            System.out.println("DEBUG: S3 Region -> " + region);

            return S3Client.builder()
                    .endpointOverride(URI.create(endpoint)) // 여기서 에러가 날 확률 90%
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true) // Supabase 필수
                            .build())
                    .build();
        } catch (Exception e) {
            // 에러 발생 시 상세 내용을 출력하도록 변경
            System.err.println("S3Client 생성 실패! 입력된 엔드포인트: " + endpoint);
            throw e;
        }
    }
}




