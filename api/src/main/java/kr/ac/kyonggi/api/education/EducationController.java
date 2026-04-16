package kr.ac.kyonggi.api.education;

import jakarta.validation.Valid;
import kr.ac.kyonggi.api.education.dto.EducationRequest;
import kr.ac.kyonggi.api.education.dto.EducationResponse;
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
@RequestMapping("/api/educations")
@RequiredArgsConstructor
public class EducationController implements EducationApi {

    private final EducationApiService educationApiService;

    @GetMapping
    public List<EducationResponse> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        return educationApiService.getAll(userDetails.getUsername());
    }

    @PostMapping
    public ResponseEntity<EducationResponse> create(
            @Valid @RequestBody EducationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(educationApiService.create(userDetails.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EducationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EducationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(educationApiService.update(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        educationApiService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
