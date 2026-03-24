package kr.ac.kyonggi.domain.service;

import kr.ac.kyonggi.domain.entity.User;

public interface UserService {

    User save(User user);

    User findByEmail(String email);

    User findById(Long id);

    boolean existsByEmail(String email);
}
