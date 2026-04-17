package kr.ac.kyonggi.api.certification;

import kr.ac.kyonggi.api.config.JpaTestConfig;
import kr.ac.kyonggi.domain.certification.Certification;
import kr.ac.kyonggi.domain.certification.CertificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
class CertificationRepositoryTest {

    @Autowired
    private CertificationRepository certificationRepository;

    @Test
    @DisplayName("findByUserId()는 해당 사용자의 자격증 목록을 반환한다")
    void findByUserId_returnsCertificationList() {
        certificationRepository.save(Certification.create(1L, "정보처리기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15)));
        certificationRepository.save(Certification.create(1L, "SQLD", "한국데이터산업진흥원",
                LocalDate.of(2024, 4, 5)));

        List<Certification> result = certificationRepository.findByUserId(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByUserId()는 다른 사용자의 자격증을 반환하지 않는다")
    void findByUserId_doesNotReturnOtherUsersCertification() {
        certificationRepository.save(Certification.create(1L, "정보처리기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15)));

        List<Certification> result = certificationRepository.findByUserId(2L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndUserId()는 본인 소유 자격증이면 반환한다")
    void findByIdAndUserId_returnsCertification_whenOwned() {
        Certification saved = certificationRepository.save(Certification.create(1L, "정보처리기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15)));

        Optional<Certification> result = certificationRepository.findByIdAndUserId(saved.getId(), 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("정보처리기사");
    }

    @Test
    @DisplayName("findByIdAndUserId()는 타인 소유 자격증이면 empty를 반환한다")
    void findByIdAndUserId_returnsEmpty_whenNotOwned() {
        Certification saved = certificationRepository.save(Certification.create(1L, "정보처리기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15)));

        Optional<Certification> result = certificationRepository.findByIdAndUserId(saved.getId(), 99L);

        assertThat(result).isEmpty();
    }
}
