package kr.ac.kyonggi.api.experience;

import jakarta.validation.Valid;
import kr.ac.kyonggi.api.experience.dto.AiSummaryStatusResponse;
import kr.ac.kyonggi.api.experience.dto.ExperienceRequest;
import kr.ac.kyonggi.api.experience.dto.ExperienceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/experiences")
@RequiredArgsConstructor
public class ExperienceController implements ExperienceApi {

    private final ExperienceApiService experienceApiService;

    @GetMapping("/project/{projectId}")
    public List<ExperienceResponse> getByProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return experienceApiService.getByProject(projectId, userDetails.getUsername());
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<ExperienceResponse> upsert(
            @PathVariable Long projectId,
            @Valid @RequestBody ExperienceRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                experienceApiService.upsert(projectId, request, userDetails.getUsername())
        );
    }

    @PostMapping("/{id}/summarize")
    public ResponseEntity<AiSummaryStatusResponse> startSummarize(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(experienceApiService.startSummarize(id, userDetails.getUsername()));
    }

    @GetMapping("/{id}/summarize")
    public ResponseEntity<AiSummaryStatusResponse> getSummaryStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(
                experienceApiService.getSummaryStatus(id, userDetails.getUsername())
        );
    }
}
