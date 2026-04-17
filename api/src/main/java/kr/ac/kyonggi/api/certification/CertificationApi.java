package kr.ac.kyonggi.api.certification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.kyonggi.api.certification.dto.CertificationRequest;
import kr.ac.kyonggi.api.certification.dto.CertificationResponse;

import java.util.List;

@Tag(name = "Certification", description = "자격증 관리 API")
public interface CertificationApi {

    @Operation(summary = "자격증 목록 조회", description = "로그인한 사용자의 자격증 목록을 반환합니다.")
    List<CertificationResponse> getAll(org.springframework.security.core.userdetails.UserDetails userDetails);

    @Operation(summary = "자격증 추가", description = "새 자격증을 추가합니다.")
    org.springframework.http.ResponseEntity<CertificationResponse> create(
            CertificationRequest request,
            org.springframework.security.core.userdetails.UserDetails userDetails);

    @Operation(summary = "자격증 수정", description = "기존 자격증을 수정합니다.")
    org.springframework.http.ResponseEntity<CertificationResponse> update(
            Long id,
            CertificationRequest request,
            org.springframework.security.core.userdetails.UserDetails userDetails);

    @Operation(summary = "자격증 삭제", description = "자격증을 삭제합니다.")
    org.springframework.http.ResponseEntity<Void> delete(
            Long id,
            org.springframework.security.core.userdetails.UserDetails userDetails);
}
