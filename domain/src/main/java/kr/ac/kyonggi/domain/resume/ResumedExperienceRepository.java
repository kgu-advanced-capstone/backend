package kr.ac.kyonggi.domain.resume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumedExperienceRepository extends JpaRepository<ResumedExperience, Long> {

    List<ResumedExperience> findByResumeId(Long resumeId);

    void deleteByResumeId(Long resumeId);
}
