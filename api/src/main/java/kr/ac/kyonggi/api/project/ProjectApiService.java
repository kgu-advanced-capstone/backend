package kr.ac.kyonggi.api.project;

import kr.ac.kyonggi.api.project.dto.CreateProjectRequest;
import kr.ac.kyonggi.api.project.dto.UpdateProjectStatusRequest;
import kr.ac.kyonggi.api.project.dto.MyProjectResponse;
import kr.ac.kyonggi.api.project.dto.ProjectDetailResponse;
import kr.ac.kyonggi.api.project.dto.ProjectListResponse;
import kr.ac.kyonggi.api.project.dto.ProjectSummaryResponse;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectCreateCommand;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectApiService {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectListResponse getProjects(String category, int page, int limit, String search) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<Project> projectPage = projectService.search(category, search, pageable);

        List<ProjectSummaryResponse> projects = projectPage.getContent().stream()
                .map(p -> ProjectSummaryResponse.from(p, projectService.getMemberCount(p.getId())))
                .toList();

        return new ProjectListResponse(projects, projectPage.getTotalElements());
    }

    public ProjectDetailResponse getProject(Long id) {
        return ProjectDetailResponse.from(projectService.getById(id), projectService.getMemberCount(id));
    }

    @Transactional
    public ProjectDetailResponse createProject(CreateProjectRequest request, String authorEmail) {
        User author = userService.getByEmail(authorEmail);

        Project project = Project.create(new ProjectCreateCommand(
                request.title(), request.description(), request.category(),
                request.skills(), request.maxMembers(), request.deadline(), author));

        Project saved = projectService.create(project);
        projectService.apply(saved.getId(), author.getId());

        return ProjectDetailResponse.from(saved, projectService.getMemberCount(saved.getId()));
    }

    @Transactional
    public void applyProject(Long projectId, String userEmail) {
        User user = userService.getByEmail(userEmail);
        projectService.apply(projectId, user.getId());
    }

    @Transactional
    public void updateStatus(Long projectId, UpdateProjectStatusRequest request, String requesterEmail) {
        User requester = userService.getByEmail(requesterEmail);
        projectService.updateStatus(projectId, requester.getId(), request.status());
    }

    public List<MyProjectResponse> getMyProjects(String userEmail) {
        User user = userService.getByEmail(userEmail);
        List<ProjectMember> memberships = projectService.getMembershipsOf(user.getId());
        return memberships.stream()
                .map(m -> MyProjectResponse.from(m, user.getId(), projectService.getMemberCount(m.getProject().getId())))
                .toList();
    }
}
