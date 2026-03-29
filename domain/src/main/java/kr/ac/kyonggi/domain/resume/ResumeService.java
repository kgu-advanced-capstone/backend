package kr.ac.kyonggi.domain.resume;

import java.util.Optional;

public interface ResumeService {

    Optional<Resume> findByUserId(Long userId);

    Resume save(Resume resume);
}
