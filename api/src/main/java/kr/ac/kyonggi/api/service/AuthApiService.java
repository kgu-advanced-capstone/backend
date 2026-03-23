package kr.ac.kyonggi.api.service;

import kr.ac.kyonggi.api.dto.request.RegisterRequest;
import kr.ac.kyonggi.api.dto.response.UserResponse;
import kr.ac.kyonggi.common.exception.UserAlreadyExistsException;
import kr.ac.kyonggi.domain.entity.User;
import kr.ac.kyonggi.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApiService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("이미 사용 중인 이메일입니다: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .build();

        User saved = userService.save(user);
        return UserResponse.from(saved);
    }

    public UserResponse findByEmail(String email) {
        return UserResponse.from(userService.findByEmail(email));
    }
}
