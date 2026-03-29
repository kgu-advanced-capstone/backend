package kr.ac.kyonggi.domain.project;

import kr.ac.kyonggi.common.exception.AlreadyAppliedException;
import kr.ac.kyonggi.common.exception.ForbiddenException;
import kr.ac.kyonggi.common.exception.ProjectNotFoundException;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Project create(Project project) {
        return projectRepository.save(project);
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
    public ProjectMember apply(Long projectId, Long userId) {
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new AlreadyAppliedException("이미 참가 신청한 프로젝트입니다.");
        }

        Project project = getById(projectId);
        User user = userService.getById(userId);

        ProjectMember projectMember = ProjectMember.of(new ProjectMemberCreateCommand(project, user));

        return projectMemberRepository.save(projectMember);
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
}
