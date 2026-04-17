package kr.ac.kyonggi.api.education;

import kr.ac.kyonggi.api.config.JpaTestConfig;
import kr.ac.kyonggi.domain.education.Education;
import kr.ac.kyonggi.domain.education.EducationRepository;
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
class EducationRepositoryTest {

    @Autowired
    private EducationRepository educationRepository;

    @Test
    @DisplayName("findByUserId()는 해당 사용자의 학력 목록을 반환한다")
    void findByUserId_returnsEducationList() {
        educationRepository.save(Education.create(1L, "경기대학교", "컴퓨터공학과", "학사",
                LocalDate.of(2020, 3, 1), null));
        educationRepository.save(Education.create(1L, "경기고등학교", null, "고졸",
                LocalDate.of(2017, 3, 1), LocalDate.of(2020, 2, 28)));

        List<Education> result = educationRepository.findByUserId(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("findByUserId()는 다른 사용자의 학력을 반환하지 않는다")
    void findByUserId_doesNotReturnOtherUsersEducation() {
        educationRepository.save(Education.create(1L, "경기대학교", "컴퓨터공학과", "학사",
                LocalDate.of(2020, 3, 1), null));

        List<Education> result = educationRepository.findByUserId(2L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByIdAndUserId()는 본인 소유 학력이면 반환한다")
    void findByIdAndUserId_returnsEducation_whenOwned() {
        Education saved = educationRepository.save(Education.create(1L, "경기대학교", "컴퓨터공학과", "학사",
                LocalDate.of(2020, 3, 1), null));

        Optional<Education> result = educationRepository.findByIdAndUserId(saved.getId(), 1L);

        assertThat(result).isPresent();
        assertThat(result.get().getSchoolName()).isEqualTo("경기대학교");
    }

    @Test
    @DisplayName("findByIdAndUserId()는 타인 소유 학력이면 empty를 반환한다")
    void findByIdAndUserId_returnsEmpty_whenNotOwned() {
        Education saved = educationRepository.save(Education.create(1L, "경기대학교", "컴퓨터공학과", "학사",
                LocalDate.of(2020, 3, 1), null));

        Optional<Education> result = educationRepository.findByIdAndUserId(saved.getId(), 99L);

        assertThat(result).isEmpty();
    }
}
