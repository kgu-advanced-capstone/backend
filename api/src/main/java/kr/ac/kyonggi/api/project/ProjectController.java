package kr.ac.kyonggi.api.project;

import jakarta.validation.Valid;
import kr.ac.kyonggi.api.project.dto.CreateProjectRequest;
import kr.ac.kyonggi.api.project.dto.UpdateProjectStatusRequest;
import kr.ac.kyonggi.api.project.dto.MyProjectResponse;
import kr.ac.kyonggi.api.project.dto.ProjectDetailResponse;
import kr.ac.kyonggi.api.project.dto.ProjectListResponse;
import kr.ac.kyonggi.api.project.ProjectApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectApiService projectApiService;

    @GetMapping
    public ProjectListResponse getProjects(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search
    ) {
        return projectApiService.getProjects(category, page, limit, search);
    }

    @GetMapping("/my")
    public List<MyProjectResponse> getMyProjects(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return projectApiService.getMyProjects(userDetails.getUsername());
    }

    @GetMapping("/{id}")
    public ProjectDetailResponse getProject(@PathVariable Long id) {
        return projectApiService.getProject(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectDetailResponse createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return projectApiService.createProject(request, userDetails.getUsername());
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<Void> applyProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        projectApiService.applyProject(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectStatusRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        projectApiService.updateStatus(id, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
