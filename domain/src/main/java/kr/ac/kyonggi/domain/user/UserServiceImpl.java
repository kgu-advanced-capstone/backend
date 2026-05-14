package kr.ac.kyonggi.domain.user;

import kr.ac.kyonggi.common.exception.UserAlreadyExistsException;
import kr.ac.kyonggi.common.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public User register(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + id));
    }

    @Override
    public Map<Long, String> getNamesByIds(List<Long> ids) {
        return userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getId, User::getName));
    }

    @Override
    public List<User> getAllByIds(List<Long> ids) {
        return userRepository.findAllById(ids);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public User findOrCreateSocialUser(UserSocialCreateCommand command) {
        return userRepository
                .findByProviderIdAndProvider(command.providerId(), command.provider())
                .orElseGet(() -> {
                    try {
                        return userRepository.save(User.ofSocial(command));
                    } catch (DataIntegrityViolationException e) {
                        // (provider_id, provider) 경쟁 조건 → 재조회
                        return userRepository
                                .findByProviderIdAndProvider(command.providerId(), command.provider())
                                .orElseGet(() -> {
                                    // email 충돌 여부 구별
                                    String email = command.email();
                                    if (email != null && !email.isBlank()) {
                                        userRepository.findByEmail(email).ifPresent(existing ->  {
                                            throw new UserAlreadyExistsException(
                                                    "이미 " + existing.getProvider().name()
                                                    + " 계정으로 가입된 이메일입니다: " + email);
                                        });
                                    }
                                    throw new UserNotFoundException(
                                            "소셜 사용자 저장 충돌 후 재조회 실패: " + command.providerId(), e);
                                });
                    }
                });
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, UpdateProfileCommand command) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + userId));
        user.updateProfile(command);
        return user;
    }
}
