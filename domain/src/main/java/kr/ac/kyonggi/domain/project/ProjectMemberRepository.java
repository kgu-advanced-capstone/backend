package kr.ac.kyonggi.domain.project;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    long countByProjectId(Long projectId);

    List<ProjectMember> findByUserId(Long userId);

    List<ProjectMember> findByProjectIdIn(List<Long> projectIds);
}
