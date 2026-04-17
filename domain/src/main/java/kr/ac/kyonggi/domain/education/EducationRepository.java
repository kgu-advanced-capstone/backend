package kr.ac.kyonggi.domain.education;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EducationRepository extends JpaRepository<Education, Long> {

    List<Education> findByUserId(Long userId);

    Optional<Education> findByIdAndUserId(Long id, Long userId);
}
