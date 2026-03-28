package kr.ac.kyonggi.domain.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {

    Project create(Project project);

    Project getById(Long id);

    Page<Project> search(String category, String keyword, Pageable pageable);

    ProjectMember apply(Long projectId, Long userId);

    void updateStatus(Long projectId, Long requesterId, ProjectStatus status);

    long getMemberCount(Long projectId);

    List<ProjectMember> getMembershipsOf(Long userId);
}
