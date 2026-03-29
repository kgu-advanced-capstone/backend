package kr.ac.kyonggi.api.profile;

import kr.ac.kyonggi.api.profile.dto.ProfileResponse;
import kr.ac.kyonggi.api.profile.dto.UpdateProfileRequest;
import kr.ac.kyonggi.domain.user.UpdateProfileCommand;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileApiService {

    private final UserService userService;

    public ProfileResponse getProfile(String email) {
        User user = userService.getByEmail(email);
        return ProfileResponse.from(user);
    }

    public ProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userService.getByEmail(email);
        User updated = userService.updateProfile(user.getId(), new UpdateProfileCommand(
                request.name(),
                request.phone(),
                request.github(),
                request.blog(),
                request.profileImage()
        ));
        return ProfileResponse.from(updated);
    }
}
