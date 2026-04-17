package kr.ac.kyonggi.domain.certification;

import kr.ac.kyonggi.common.exception.CertificationNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificationServiceImpl implements CertificationService {

    private final CertificationRepository certificationRepository;

    @Override
    public List<Certification> getAllByUserId(Long userId) {
        return certificationRepository.findByUserId(userId);
    }

    @Override
    public Certification getByIdAndUserId(Long id, Long userId) {
        return certificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CertificationNotFoundException("자격증 정보를 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional
    public Certification save(Certification certification) {
        return certificationRepository.save(certification);
    }

    @Override
    @Transactional
    public void delete(Certification certification) {
        certificationRepository.delete(certification);
    }
}
