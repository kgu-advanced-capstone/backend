package kr.ac.kyonggi.domain.education;

import java.util.List;

public interface EducationService {

    List<Education> getAllByUserId(Long userId);

    Education getByIdAndUserId(Long id, Long userId);

    Education save(Education education);

    void delete(Education education);
}
