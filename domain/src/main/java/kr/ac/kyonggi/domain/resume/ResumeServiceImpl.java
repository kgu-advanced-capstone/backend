package kr.ac.kyonggi.domain.resume;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;

    @Override
    public Optional<Resume> findByUserId(Long userId) {
        return resumeRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Resume save(Resume resume) {
        return resumeRepository.save(resume);
    }
}
