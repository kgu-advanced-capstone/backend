package kr.ac.kyonggi.domain.education;

import kr.ac.kyonggi.common.exception.EducationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;

    @Override
    public List<Education> getAllByUserId(Long userId) {
        return educationRepository.findByUserId(userId);
    }

    @Override
    public Education getByIdAndUserId(Long id, Long userId) {
        return educationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EducationNotFoundException("학력 정보를 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional
    public Education save(Education education) {
        return educationRepository.save(education);
    }

    @Override
    @Transactional
    public void delete(Education education) {
        educationRepository.delete(education);
    }
}
