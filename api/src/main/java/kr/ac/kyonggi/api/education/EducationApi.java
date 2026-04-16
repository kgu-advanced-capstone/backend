package kr.ac.kyonggi.api.education;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.ac.kyonggi.api.education.dto.EducationRequest;
import kr.ac.kyonggi.api.education.dto.EducationResponse;

import java.util.List;

@Tag(name = "Education", description = "학력 관리 API")
public interface EducationApi {

    @Operation(summary = "학력 목록 조회", description = "로그인한 사용자의 학력 목록을 반환합니다.")
    List<EducationResponse> getAll(org.springframework.security.core.userdetails.UserDetails userDetails);

    @Operation(summary = "학력 추가", description = "새 학력을 추가합니다.")
    org.springframework.http.ResponseEntity<EducationResponse> create(
            EducationRequest request,
            org.springframework.security.core.userdetails.UserDetails userDetails);

    @Operation(summary = "학력 수정", description = "기존 학력을 수정합니다.")
    org.springframework.http.ResponseEntity<EducationResponse> update(
            Long id,
            EducationRequest request,
            org.springframework.security.core.userdetails.UserDetails userDetails);

    @Operation(summary = "학력 삭제", description = "학력을 삭제합니다.")
    org.springframework.http.ResponseEntity<Void> delete(
            Long id,
            org.springframework.security.core.userdetails.UserDetails userDetails);
}
