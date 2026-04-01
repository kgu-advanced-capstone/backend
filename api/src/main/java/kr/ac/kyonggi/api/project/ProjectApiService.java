package kr.ac.kyonggi.api.project;

import kr.ac.kyonggi.api.project.dto.*;
import kr.ac.kyonggi.domain.notification.NotificationCreatedEvent;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectCreateCommand;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectApiService {

    private final ProjectService projectService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public ProjectListResponse getProjects(String category, int page, int limit, String search) {
        PageRequest pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<Project> projectPage = projectService.search(category, search, pageable);

        List<Long> projectIds = projectPage.getContent().stream().map(Project::getId).toList();
        Map<Long, Long> memberCounts = projectService.getMemberCounts(projectIds);
        List<Long> authorIds = projectPage.getContent().stream().map(Project::getAuthorId).toList();
        Map<Long, String> authorNames = userService.getNamesByIds(authorIds);

        List<ProjectSummaryResponse> projects = projectPage.getContent().stream()
                .map(p -> ProjectSummaryResponse.from(p, memberCounts.getOrDefault(p.getId(), 0L),
                        authorNames.getOrDefault(p.getAuthorId(), "")))
                .toList();

        return new ProjectListResponse(projects, projectPage.getTotalElements());
    }

    public ProjectDetailResponse getProject(Long id) {
        Project project = projectService.getById(id);
        String authorName = userService.getById(project.getAuthorId()).getName();
        return ProjectDetailResponse.from(project, projectService.getMemberCount(id), authorName);
    }

    @Transactional
    public ProjectDetailResponse createProject(CreateProjectRequest request, String authorEmail) {
        User author = userService.getByEmail(authorEmail);

        Project project = Project.create(new ProjectCreateCommand(
                request.title(), request.description(), request.category(),
                request.skills(), request.maxMembers(), request.deadline(),
                author.getId()));

        Project saved = projectService.create(project);
        projectService.apply(saved.getId(), author.getId());
        eventPublisher.publishEvent(new NotificationCreatedEvent(
                author.getId(), "\"" + saved.getTitle() + "\" 프로젝트에 참가했습니다."));

        return ProjectDetailResponse.from(saved, projectService.getMemberCount(saved.getId()), author.getName());
    }

    @Transactional
    public void applyProject(Long projectId, String userEmail) {
        User user = userService.getByEmail(userEmail);

        Project project = projectService.apply(projectId, user.getId());

        eventPublisher.publishEvent(new NotificationCreatedEvent(
                user.getId(), "\"" + project.getTitle() + "\" 프로젝트에 참가했습니다."));
    }

    @Transactional
    public void updateStatus(Long projectId, UpdateProjectStatusRequest request, String requesterEmail) {
        User requester = userService.getByEmail(requesterEmail);
        projectService.updateStatus(projectId, requester.getId(), request.status());
    }

    public List<MyProjectResponse> getMyProjects(String userEmail) {
        User user = userService.getByEmail(userEmail);
        List<ProjectMember> memberships = projectService.getMembershipsOf(user.getId());

        List<Long> projectIds = memberships.stream().map(ProjectMember::getProjectId).toList();
        Map<Long, Project> projectMap = projectService.getAllByIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, p -> p));

        Map<Long, Long> memberCounts = projectService.getMemberCounts(projectIds);
        List<Long> authorIds = projectMap.values().stream().map(Project::getAuthorId).toList();
        Map<Long, String> authorNames = userService.getNamesByIds(authorIds);

        return memberships.stream()
                .filter(m -> projectMap.containsKey(m.getProjectId()))
                .map(m -> {
                    Project p = projectMap.get(m.getProjectId());
                    return MyProjectResponse.from(p, m, user.getId(),
                            memberCounts.getOrDefault(p.getId(), 0L),
                            authorNames.getOrDefault(p.getAuthorId(), ""));
                })
                .toList();
    }
}
