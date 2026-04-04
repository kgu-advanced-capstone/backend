package kr.ac.kyonggi.domain.experience;

import kr.ac.kyonggi.common.exception.ExperienceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperienceRepository experienceRepository;

    @Override
    public List<Experience> getByProjectIdAndUserId(Long projectId, Long userId) {
        return experienceRepository.findByProjectIdAndUserId(projectId, userId)
                .map(List::of)
                .orElse(List.of());
    }

    @Override
    public Optional<Experience> findByProjectIdAndUserId(Long projectId, Long userId) {
        return experienceRepository.findByProjectIdAndUserId(projectId, userId);
    }

    @Override
    public Experience getById(Long id) {
        return experienceRepository.findById(id)
                .orElseThrow(() -> new ExperienceNotFoundException("경험 기록을 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional
    public Experience getByIdWithLock(Long id) {
        return experienceRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ExperienceNotFoundException("경험 기록을 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional
    public Experience save(Experience experience) {
        return experienceRepository.save(experience);
    }

    @Override
    public Map<Long, Experience> findByProjectIdsAndUserId(List<Long> projectIds, Long userId) {
        return experienceRepository.findByProjectIdInAndUserId(projectIds, userId).stream()
                .collect(Collectors.toMap(Experience::getProjectId, e -> e));
    }
}
