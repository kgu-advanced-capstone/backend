package kr.ac.kyonggi.domain.experience;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ExperienceService {

    List<Experience> getByProjectIdAndUserId(Long projectId, Long userId);

    Optional<Experience> findByProjectIdAndUserId(Long projectId, Long userId);

    Experience getById(Long id);

    Experience save(Experience experience);

    Map<Long, Experience> findByProjectIdsAndUserId(List<Long> projectIds, Long userId);
}
