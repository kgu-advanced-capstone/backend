package kr.ac.kyonggi.api.certification;

import jakarta.validation.Valid;
import kr.ac.kyonggi.api.certification.dto.CertificationRequest;
import kr.ac.kyonggi.api.certification.dto.CertificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/certifications")
@RequiredArgsConstructor
public class CertificationController implements CertificationApi {

    private final CertificationApiService certificationApiService;

    @GetMapping
    public List<CertificationResponse> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        return certificationApiService.getAll(userDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<CertificationResponse> create(
            @Valid @RequestBody CertificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(certificationApiService.create(userDetails.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CertificationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CertificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(certificationApiService.update(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        certificationApiService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
