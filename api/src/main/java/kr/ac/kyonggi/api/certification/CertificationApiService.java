package kr.ac.kyonggi.api.certification;

import kr.ac.kyonggi.api.certification.dto.CertificationRequest;
import kr.ac.kyonggi.api.certification.dto.CertificationResponse;
import kr.ac.kyonggi.domain.certification.Certification;
import kr.ac.kyonggi.domain.certification.CertificationService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificationApiService {

    private final CertificationService certificationService;
    private final UserService userService;

    public List<CertificationResponse> getAll(String email) {
        User user = userService.getByEmail(email);
        return certificationService.getAllByUserId(user.getId()).stream()
                .map(CertificationResponse::from)
                .toList();
    }

    @Transactional
    public CertificationResponse create(String email, CertificationRequest request) {
        User user = userService.getByEmail(email);
        Certification certification = Certification.create(
                user.getId(),
                request.name(),
                request.issuingOrganization(),
                request.issuedDate()
        );
        return CertificationResponse.from(certificationService.save(certification));
    }

    @Transactional
    public CertificationResponse update(String email, Long id, CertificationRequest request) {
        User user = userService.getByEmail(email);
        Certification certification = certificationService.getByIdAndUserId(id, user.getId());
        certification.update(request.name(), request.issuingOrganization(), request.issuedDate());
        return CertificationResponse.from(certificationService.save(certification));
    }

    @Transactional
    public void delete(String email, Long id) {
        User user = userService.getByEmail(email);
        Certification certification = certificationService.getByIdAndUserId(id, user.getId());
        certificationService.delete(certification);
    }
}
