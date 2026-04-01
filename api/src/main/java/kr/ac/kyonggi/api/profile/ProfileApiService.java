package kr.ac.kyonggi.api.profile;

import kr.ac.kyonggi.api.profile.dto.ProfileResponse;
import kr.ac.kyonggi.api.profile.dto.UpdateProfileRequest;
import kr.ac.kyonggi.common.exception.StorageException;
import kr.ac.kyonggi.domain.storage.FileStorage;
import kr.ac.kyonggi.domain.user.UpdateProfileCommand;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProfileApiService {

    private final UserService userService;
    private final FileStorage fileStorage;

    public ProfileResponse getProfile(String email) {
        User user = userService.getByEmail(email);
        return ProfileResponse.from(user);
    }

    public ProfileResponse updateProfile(String email, UpdateProfileRequest request, MultipartFile profileImage) {
        User user = userService.getByEmail(email);

        String profileImageUrl = user.getProfileImage();

        // 새 이미지가 업로드된 경우
        if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 이미지가 있다면 삭제
            if (profileImageUrl != null) {
                fileStorage.delete(profileImageUrl);
            }
            // 새 이미지 업로드
            try {
                profileImageUrl = fileStorage.upload(
                        profileImage.getInputStream(),
                        profileImage.getOriginalFilename(),
                        profileImage.getContentType()
                );
            } catch (IOException e) {
                throw new StorageException("프로필 이미지 업로드 중 오류가 발생했습니다.", e);
            }
        }

        User updated = userService.updateProfile(user.getId(), new UpdateProfileCommand(
                request.name(),
                request.phone(),
                request.github(),
                request.blog(),
                profileImageUrl
        ));
        return ProfileResponse.from(updated);
    }
}
