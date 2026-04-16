package kr.ac.kyonggi.api.education;

import kr.ac.kyonggi.api.education.dto.EducationRequest;
import kr.ac.kyonggi.api.education.dto.EducationResponse;
import kr.ac.kyonggi.domain.education.Education;
import kr.ac.kyonggi.domain.education.EducationService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EducationApiService {

    private final EducationService educationService;
    private final UserService userService;

    public List<EducationResponse> getAll(String email) {
        User user = userService.getByEmail(email);
        return educationService.getAllByUserId(user.getId()).stream()
                .map(EducationResponse::from)
                .toList();
    }

    @Transactional
    public EducationResponse create(String email, EducationRequest request) {
        User user = userService.getByEmail(email);
        Education education = Education.create(
                user.getId(),
                request.schoolName(),
                request.major(),
                request.degree(),
                request.startDate(),
                request.endDate()
        );
        return EducationResponse.from(educationService.save(education));
    }

    @Transactional
    public EducationResponse update(String email, Long id, EducationRequest request) {
        User user = userService.getByEmail(email);
        Education education = educationService.getByIdAndUserId(id, user.getId());
        education.update(request.schoolName(), request.major(), request.degree(),
                request.startDate(), request.endDate());
        return EducationResponse.from(educationService.save(education));
    }

    @Transactional
    public void delete(String email, Long id) {
        User user = userService.getByEmail(email);
        Education education = educationService.getByIdAndUserId(id, user.getId());
        educationService.delete(education);
    }
}
