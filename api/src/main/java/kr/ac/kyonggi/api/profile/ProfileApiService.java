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

        String oldProfileImageUrl = user.getProfileImage();
        String newProfileImageUrl = oldProfileImageUrl;

        // 1. 새 이미지가 업로드된 경우 먼저 업로드 수행
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                newProfileImageUrl = fileStorage.upload(
                        profileImage.getInputStream(),
                        profileImage.getOriginalFilename(),
                        profileImage.getContentType()
                );
            } catch (IOException e) {
                throw new StorageException("프로필 이미지 업로드 중 오류가 발생했습니다.", e);
            }
        }

        // 2. 유저 정보 업데이트 (DB 반영)
        User updated = userService.updateProfile(user.getId(), new UpdateProfileCommand(
                request.name(),
                request.phone(),
                request.github(),
                request.blog(),
                newProfileImageUrl
        ));

        // 3. 업데이트 성공 후, 새 이미지가 정상적으로 등록되었고 기존 이미지가 있었다면 기존 이미지 삭제
        if (profileImage != null && !profileImage.isEmpty() && oldProfileImageUrl != null) {
            fileStorage.delete(oldProfileImageUrl);
        }

        return ProfileResponse.from(updated);
    }
}
