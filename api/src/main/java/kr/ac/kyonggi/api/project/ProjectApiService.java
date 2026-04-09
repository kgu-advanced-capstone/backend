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
        long totalCount = projectService.countProjects(category, search);
        List<Project> content = projectService.searchContent(category, search, pageable);

        List<Long> projectIds = content.stream().map(Project::getId).toList();
        Map<Long, Long> memberCounts = projectService.getMemberCounts(projectIds);
        List<Long> authorIds = content.stream().map(Project::getAuthorId).toList();
        Map<Long, String> authorNames = userService.getNamesByIds(authorIds);

        List<ProjectSummaryResponse> projects = content.stream()
                .map(p -> ProjectSummaryResponse.from(p, memberCounts.getOrDefault(p.getId(), 0L),
                        authorNames.getOrDefault(p.getAuthorId(), "")))
                .toList();

        return new ProjectListResponse(projects, totalCount);
    }

    public ProjectDetailResponse getProject(Long id) {
        Project project = projectService.getById(id);
        String authorName = userService.getById(project.getAuthorId()).getName();
        List<ParticipantResponse> participants = buildParticipants(id);
        return ProjectDetailResponse.from(project, participants, authorName);
    }

    @Transactional
    public ProjectDetailResponse createProject(CreateProjectRequest request, String authorEmail) {
        User author = userService.getByEmail(authorEmail);

        Project project = Project.create(new ProjectCreateCommand(
                request.title(), request.description(), request.category(),
                request.skills(), request.maxMembers(), request.deadline(),
                author.getId()));

        Project saved = projectService.create(project);
        eventPublisher.publishEvent(new NotificationCreatedEvent(
                author.getId(), "\"" + saved.getTitle() + "\" 프로젝트에 참가했습니다."));

        List<ParticipantResponse> participants = buildParticipants(saved.getId());
        return ProjectDetailResponse.from(saved, participants, author.getName());
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

    private List<ParticipantResponse> buildParticipants(Long projectId) {
        List<ProjectMember> members = projectService.getParticipants(projectId);
        List<Long> userIds = members.stream().map(ProjectMember::getUserId).toList();
        Map<Long, User> userMap = userService.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return members.stream()
                .filter(m -> userMap.containsKey(m.getUserId()))
                .map(m -> ParticipantResponse.of(userMap.get(m.getUserId()), m))
                .toList();
    }

    private Map<Long, List<ParticipantResponse>> buildParticipantsMap(List<Long> projectIds) {
        List<ProjectMember> members = projectService.getParticipantsByProjectIds(projectIds);
        List<Long> userIds = members.stream().map(ProjectMember::getUserId).toList();
        Map<Long, User> userMap = userService.getAllByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        return members.stream()
                .filter(m -> userMap.containsKey(m.getUserId()))
                .collect(Collectors.groupingBy(
                        ProjectMember::getProjectId,
                        Collectors.mapping(m -> ParticipantResponse.of(userMap.get(m.getUserId()), m), Collectors.toList())
                ));
    }

    public List<MyProjectResponse> getMyProjects(String userEmail) {
        User user = userService.getByEmail(userEmail);
        List<ProjectMember> memberships = projectService.getMembershipsOf(user.getId());

        List<Long> projectIds = memberships.stream().map(ProjectMember::getProjectId).toList();
        Map<Long, Project> projectMap = projectService.getAllByIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, p -> p));

        Map<Long, List<ParticipantResponse>> participantsMap = buildParticipantsMap(projectIds);
        List<Long> authorIds = projectMap.values().stream().map(Project::getAuthorId).toList();
        Map<Long, String> authorNames = userService.getNamesByIds(authorIds);

        return memberships.stream()
                .filter(m -> projectMap.containsKey(m.getProjectId()))
                .map(m -> {
                    Project p = projectMap.get(m.getProjectId());
                    return MyProjectResponse.from(p, m, user.getId(),
                            participantsMap.getOrDefault(p.getId(), List.of()),
                            authorNames.getOrDefault(p.getAuthorId(), ""));
                })
                .toList();
    }
}
