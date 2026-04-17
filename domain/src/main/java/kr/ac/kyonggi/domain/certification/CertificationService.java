package kr.ac.kyonggi.domain.certification;

import java.util.List;

public interface CertificationService {

    List<Certification> getAllByUserId(Long userId);

    Certification getByIdAndUserId(Long id, Long userId);

    Certification save(Certification certification);

    void delete(Certification certification);
}
