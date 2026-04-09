package kr.ac.kyonggi.domain.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProjectService {

    Project create(Project project);

    Project getById(Long id);

    Page<Project> search(String category, String keyword, Pageable pageable);

    Page<Long> searchIds(String category, String keyword, Pageable pageable);

    ProjectData getProjectData(Long id);

    Project apply(Long projectId, Long userId);

    void updateStatus(Long projectId, Long requesterId, ProjectStatus status);

    long getMemberCount(Long projectId);

    List<ProjectMember> getMembershipsOf(Long userId);

    List<Project> getAllByIds(List<Long> ids);

    Map<Long, Long> getMemberCounts(List<Long> projectIds);

    List<ProjectMember> getParticipants(Long projectId);

    List<ProjectMember> getParticipantsByProjectIds(List<Long> projectIds);
}
