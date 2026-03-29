package kr.ac.kyonggi.api.auth;

import kr.ac.kyonggi.api.auth.dto.RegisterRequest;
import kr.ac.kyonggi.api.auth.dto.UserResponse;
import kr.ac.kyonggi.common.exception.UserAlreadyExistsException;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserCreateCommand;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApiService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        if (userService.isEmailTaken(request.email())) {
            throw new UserAlreadyExistsException("이미 사용 중인 이메일입니다: " + request.email());
        }

        User user = User.create(new UserCreateCommand(
                request.email(), passwordEncoder.encode(request.password()), request.name(), null));

        User saved = userService.register(user);
        return UserResponse.from(saved);
    }

    public UserResponse findByEmail(String email) {
        return UserResponse.from(userService.getByEmail(email));
    }
}
