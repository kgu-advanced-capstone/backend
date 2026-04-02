package kr.ac.kyonggi.domain.user;

import java.util.List;
import java.util.Map;

public interface UserService {

    User register(User user);

    User getByEmail(String email);

    User getById(Long id);

    Map<Long, String> getNamesByIds(List<Long> ids);

    List<User> getAllByIds(List<Long> ids);

    boolean isEmailTaken(String email);

    User updateProfile(Long userId, UpdateProfileCommand command);
}
