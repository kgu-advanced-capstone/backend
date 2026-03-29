package kr.ac.kyonggi.domain.experience;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    Optional<Experience> findByProjectIdAndUserId(Long projectId, Long userId);
}
