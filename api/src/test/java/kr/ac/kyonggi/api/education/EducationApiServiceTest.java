package kr.ac.kyonggi.api.education;

import kr.ac.kyonggi.api.education.dto.EducationRequest;
import kr.ac.kyonggi.api.education.dto.EducationResponse;
import kr.ac.kyonggi.common.exception.EducationNotFoundException;
import kr.ac.kyonggi.domain.education.Education;
import kr.ac.kyonggi.domain.education.EducationService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import kr.ac.kyonggi.domain.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EducationApiServiceTest {

    @Mock private EducationService educationService;
    @Mock private UserService userService;

    @InjectMocks
    private EducationApiService educationApiService;

    private static final String EMAIL = "test@test.com";
    private static final Long USER_ID = 1L;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(new UserCreateCommand(EMAIL, "pw", "홍길동", null, null));
        ReflectionTestUtils.setField(user, "id", USER_ID);
    }

    @Test
    @DisplayName("getAll()은 사용자의 학력 목록을 반환한다")
    void getAll_returnsEducationList() {
        Education edu = Education.create(USER_ID, "경기대학교", "컴퓨터공학과", "학사",
                LocalDate.of(2020, 3, 1), null);

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(educationService.getAllByUserId(USER_ID)).willReturn(List.of(edu));

        List<EducationResponse> result = educationApiService.getAll(EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).schoolName()).isEqualTo("경기대학교");
    }

    @Test
    @DisplayName("create()는 학력을 저장하고 반환한다")
    void create_savesAndReturnsEducation() {
        EducationRequest request = new EducationRequest("경기대학교", "컴퓨터공학과", "학사",
                LocalDate.of(2020, 3, 1), null);
        Education saved = Education.create(USER_ID, "경기대학교", "컴퓨터공학과", "학사",
                LocalDate.of(2020, 3, 1), null);

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(educationService.save(any())).willReturn(saved);

        EducationResponse result = educationApiService.create(EMAIL, request);

        assertThat(result.schoolName()).isEqualTo("경기대학교");
        verify(educationService).save(any());
    }

    @Test
    @DisplayName("update()는 본인 학력을 수정하고 반환한다")
    void update_updatesAndReturnsEducation() {
        Education existing = Education.create(USER_ID, "경기대학교", "컴퓨터공학과", "학사",
                LocalDate.of(2020, 3, 1), null);
        ReflectionTestUtils.setField(existing, "id", 10L);
        EducationRequest request = new EducationRequest("경기대학교", "소프트웨어공학과", "학사",
                LocalDate.of(2020, 3, 1), null);

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(educationService.getByIdAndUserId(10L, USER_ID)).willReturn(existing);
        given(educationService.save(existing)).willReturn(existing);

        EducationResponse result = educationApiService.update(EMAIL, 10L, request);

        assertThat(result.major()).isEqualTo("소프트웨어공학과");
        verify(educationService).save(existing);
    }

    @Test
    @DisplayName("update()는 본인 소유가 아니면 EducationNotFoundException을 던진다")
    void update_throwsNotFoundException_whenNotOwned() {
        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(educationService.getByIdAndUserId(99L, USER_ID))
                .willThrow(new EducationNotFoundException("학력 정보를 찾을 수 없습니다: 99"));

        assertThatThrownBy(() -> educationApiService.update(EMAIL, 99L,
                new EducationRequest("X", null, null, LocalDate.now(), null)))
                .isInstanceOf(EducationNotFoundException.class);
    }

    @Test
    @DisplayName("delete()는 본인 학력을 삭제한다")
    void delete_deletesEducation() {
        Education existing = Education.create(USER_ID, "경기대학교", "컴퓨터공학과", "학사",
                LocalDate.of(2020, 3, 1), null);
        ReflectionTestUtils.setField(existing, "id", 10L);

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(educationService.getByIdAndUserId(10L, USER_ID)).willReturn(existing);

        educationApiService.delete(EMAIL, 10L);

        verify(educationService).delete(existing);
    }
}
