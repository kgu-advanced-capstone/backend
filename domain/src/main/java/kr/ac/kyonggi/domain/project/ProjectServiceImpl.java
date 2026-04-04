package kr.ac.kyonggi.domain.project;

import kr.ac.kyonggi.common.exception.AlreadyAppliedException;
import kr.ac.kyonggi.common.exception.ForbiddenException;
import kr.ac.kyonggi.common.exception.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public Project create(Project project) {
        Project saved = projectRepository.save(project);
        projectMemberRepository.save
                (ProjectMember.of(new ProjectMemberCreateCommand(saved.getId(), saved.getAuthorId())));
        return saved;
    }

    @Override
    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + id));
    }

    @Override
    public Page<Project> search(String category, String keyword, Pageable pageable) {
        return projectRepository.findWithFilters(category, keyword, pageable);
    }

    @Override
    @Transactional
    public Project apply(Long projectId, Long userId) {
        Project project = projectRepository.findByIdWithLock(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("프로젝트를 찾을 수 없습니다: " + projectId));

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new AlreadyAppliedException("이미 참가 신청한 프로젝트입니다.");
        }

        project.addMember();

        projectMemberRepository.save(ProjectMember.of(new ProjectMemberCreateCommand(projectId, userId)));

        return project;
    }

    @Override
    @Transactional
    public void updateStatus(Long projectId, Long requesterId, ProjectStatus status) {
        Project project = getById(projectId);

        if (!project.isAuthor(requesterId)) {
            throw new ForbiddenException("프로젝트 작성자만 상태를 변경할 수 있습니다.");
        }

        project.updateStatus(status);
    }

    @Override
    public long getMemberCount(Long projectId) {
        return projectMemberRepository.countByProjectId(projectId);
    }

    @Override
    public List<ProjectMember> getMembershipsOf(Long userId) {
        return projectMemberRepository.findByUserId(userId);
    }

    @Override
    public List<Project> getAllByIds(List<Long> ids) {
        return projectRepository.findAllById(ids);
    }

    @Override
    public Map<Long, Long> getMemberCounts(List<Long> projectIds) {
        return projectMemberRepository.findByProjectIdIn(projectIds).stream()
                .collect(Collectors.groupingBy(ProjectMember::getProjectId, Collectors.counting()));
    }

    @Override
    public List<ProjectMember> getParticipants(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }

    @Override
    public List<ProjectMember> getParticipantsByProjectIds(List<Long> projectIds) {
        return projectMemberRepository.findByProjectIdIn(projectIds);
    }
}
