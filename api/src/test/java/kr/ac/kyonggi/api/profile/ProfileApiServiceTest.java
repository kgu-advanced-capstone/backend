package kr.ac.kyonggi.api.profile;

import kr.ac.kyonggi.api.profile.dto.ProfileResponse;
import kr.ac.kyonggi.api.profile.dto.UpdateProfileRequest;
import kr.ac.kyonggi.domain.storage.FileStorage;
import kr.ac.kyonggi.domain.user.UpdateProfileCommand;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileApiServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private ProfileApiService profileApiService;

    @Test
    @DisplayName("getProfile()은 UserService.getByEmail()을 호출하고 ProfileResponse로 변환한다")
    void getProfile_delegatesAndReturnsProfileResponse() {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn("test@test.com");
        when(user.getName()).thenReturn("홍길동");
        when(user.getPhone()).thenReturn("010-0000-0000");
        when(user.getGithub()).thenReturn("https://github.com/test");
        when(userService.getByEmail("test@test.com")).thenReturn(user);

        ProfileResponse result = profileApiService.getProfile("test@test.com");

        assertThat(result.email()).isEqualTo("test@test.com");
        assertThat(result.name()).isEqualTo("홍길동");
        assertThat(result.github()).isEqualTo("https://github.com/test");
        verify(userService).getByEmail("test@test.com");
    }

    @Test
    @DisplayName("updateProfile() 시 새 이미지가 업로드되면 기존 이미지를 삭제하고 새 이미지를 저장한다")
    void updateProfile_whenNewImageUploaded_deletesOldAndSavesNew() throws IOException {
        // given
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getProfileImage()).thenReturn("old-image.png");
        when(userService.getByEmail("test@test.com")).thenReturn(user);
        when(userService.updateProfile(eq(1L), any(UpdateProfileCommand.class))).thenReturn(mock(User.class));
        when(fileStorage.upload(any(InputStream.class), anyString(), anyString())).thenReturn("new-image-url.png");

        UpdateProfileRequest request = new UpdateProfileRequest("이름", null, null, null);
        MockMultipartFile profileImage = new MockMultipartFile("profileImage", "new.png", "image/png", "content".getBytes());

        // when
        profileApiService.updateProfile("test@test.com", request, profileImage);

        // then
        verify(fileStorage).delete("old-image.png");
        verify(fileStorage).upload(any(InputStream.class), eq("new.png"), eq("image/png"));
    }

    @Test
    @DisplayName("updateProfile() 시 이미지가 없으면 삭제 로직이 실행되지 않는다")
    void updateProfile_whenNoImage_doesNotDelete() {
        // given
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getProfileImage()).thenReturn("old-image.png");
        when(userService.getByEmail("test@test.com")).thenReturn(user);
        when(userService.updateProfile(eq(1L), any(UpdateProfileCommand.class))).thenReturn(mock(User.class));

        UpdateProfileRequest request = new UpdateProfileRequest("이름", null, null, null);

        // when
        profileApiService.updateProfile("test@test.com", request, null);

        // then
        verify(fileStorage, never()).delete(anyString());
        verify(fileStorage, never()).upload(any(InputStream.class), anyString(), anyString());
    }

    @Test
    @DisplayName("updateProfile()은 올바른 UpdateProfileCommand로 UserService.updateProfile()을 호출한다")
    void updateProfile_callsUpdateProfileWithCorrectCommand() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        User updated = mock(User.class);
        when(userService.getByEmail("test@test.com")).thenReturn(user);
        when(userService.updateProfile(eq(1L), any(UpdateProfileCommand.class))).thenReturn(updated);

        profileApiService.updateProfile("test@test.com",
                new UpdateProfileRequest("새이름", "010-1234-5678", null, null), null);

        ArgumentCaptor<UpdateProfileCommand> captor = ArgumentCaptor.forClass(UpdateProfileCommand.class);
        verify(userService).updateProfile(eq(1L), captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("새이름");
        assertThat(captor.getValue().phone()).isEqualTo("010-1234-5678");
    }
}
