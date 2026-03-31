package kr.ac.kyonggi.api.resume;

import kr.ac.kyonggi.api.resume.dto.ResumeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController implements ResumeApi {

    private final ResumeApiService resumeApiService;

    @GetMapping
    public ResponseEntity<ResumeResponse> getResume(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(resumeApiService.getResume(userDetails.getUsername()));
    }

    @PostMapping("/generate")
    public ResponseEntity<Void> generate(@AuthenticationPrincipal UserDetails userDetails) {
        resumeApiService.generate(userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
