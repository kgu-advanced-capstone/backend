package kr.ac.kyonggi.domain.experience;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    Optional<Experience> findByProjectIdAndUserId(Long projectId, Long userId);

    List<Experience> findByProjectIdInAndUserId(List<Long> projectIds, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Experience e WHERE e.id = :id")
    Optional<Experience> findByIdWithLock(Long id);
}
