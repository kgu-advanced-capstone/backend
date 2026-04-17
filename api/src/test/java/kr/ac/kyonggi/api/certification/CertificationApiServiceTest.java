package kr.ac.kyonggi.api.certification;

import kr.ac.kyonggi.api.certification.dto.CertificationRequest;
import kr.ac.kyonggi.api.certification.dto.CertificationResponse;
import kr.ac.kyonggi.common.exception.CertificationNotFoundException;
import kr.ac.kyonggi.domain.certification.Certification;
import kr.ac.kyonggi.domain.certification.CertificationService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CertificationApiServiceTest {

    @Mock private CertificationService certificationService;
    @Mock private UserService userService;

    @InjectMocks
    private CertificationApiService certificationApiService;

    private static final String EMAIL = "test@test.com";
    private static final Long USER_ID = 1L;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(new UserCreateCommand(EMAIL, "pw", "홍길동", null, null));
        ReflectionTestUtils.setField(user, "id", USER_ID);
    }

    @Test
    @DisplayName("getAll()은 사용자의 자격증 목록을 반환한다")
    void getAll_returnsCertificationList() {
        Certification cert = Certification.create(USER_ID, "정보처리기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15));

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(certificationService.getAllByUserId(USER_ID)).willReturn(List.of(cert));

        List<CertificationResponse> result = certificationApiService.getAll(EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("정보처리기사");
    }

    @Test
    @DisplayName("create()는 자격증을 저장하고 반환한다")
    void create_savesAndReturnsCertification() {
        CertificationRequest request = new CertificationRequest("정보처리기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15));
        Certification saved = Certification.create(USER_ID, "정보처리기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15));

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(certificationService.save(any())).willReturn(saved);

        CertificationResponse result = certificationApiService.create(EMAIL, request);

        assertThat(result.name()).isEqualTo("정보처리기사");
        verify(certificationService).save(any());
    }

    @Test
    @DisplayName("update()는 본인 자격증을 수정하고 반환한다")
    void update_updatesAndReturnsCertification() {
        Certification existing = Certification.create(USER_ID, "정보처리기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15));
        ReflectionTestUtils.setField(existing, "id", 10L);
        CertificationRequest request = new CertificationRequest("정보처리산업기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15));

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(certificationService.getByIdAndUserId(10L, USER_ID)).willReturn(existing);
        given(certificationService.save(existing)).willReturn(existing);

        CertificationResponse result = certificationApiService.update(EMAIL, 10L, request);

        assertThat(result.name()).isEqualTo("정보처리산업기사");
        verify(certificationService).save(existing);
    }

    @Test
    @DisplayName("update()는 본인 소유가 아니면 CertificationNotFoundException을 던진다")
    void update_throwsNotFoundException_whenNotOwned() {
        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(certificationService.getByIdAndUserId(99L, USER_ID))
                .willThrow(new CertificationNotFoundException("자격증 정보를 찾을 수 없습니다: 99"));

        assertThatThrownBy(() -> certificationApiService.update(EMAIL, 99L,
                new CertificationRequest("X", null, LocalDate.now())))
                .isInstanceOf(CertificationNotFoundException.class);
    }

    @Test
    @DisplayName("delete()는 본인 자격증을 삭제한다")
    void delete_deletesCertification() {
        Certification existing = Certification.create(USER_ID, "정보처리기사", "한국산업인력공단",
                LocalDate.of(2023, 11, 15));
        ReflectionTestUtils.setField(existing, "id", 10L);

        given(userService.getByEmail(EMAIL)).willReturn(user);
        given(certificationService.getByIdAndUserId(10L, USER_ID)).willReturn(existing);

        certificationApiService.delete(EMAIL, 10L);

        verify(certificationService).delete(existing);
    }
}
