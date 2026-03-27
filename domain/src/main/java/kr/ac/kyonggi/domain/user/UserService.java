package kr.ac.kyonggi.domain.user;

public interface UserService {

    User register(User user);

    User getByEmail(String email);

    User getById(Long id);

    boolean isEmailTaken(String email);
}
