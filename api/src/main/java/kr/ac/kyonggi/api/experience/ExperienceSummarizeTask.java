package kr.ac.kyonggi.api.experience;

import kr.ac.kyonggi.domain.experience.Experience;
import kr.ac.kyonggi.domain.experience.ExperienceService;
import kr.ac.kyonggi.domain.experience.ExperienceSummarizer;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExperienceSummarizeTask {

    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final ExperienceSummarizer experienceSummarizer;

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void markFailed(Long experienceId) {
        try {
            Experience experience = experienceService.getById(experienceId);
            experience.failSummarizing();
            experienceService.save(experience);
        } catch (Exception e) {
            log.error("AI 요약 실패 상태 처리 중 오류 experienceId={}", experienceId, e);
        }
    }

    @Async("asyncExecutor")
    @Transactional
    public void run(Long experienceId, Long projectId) {
        Experience experience;
        try {
            experience = experienceService.getById(experienceId);
        } catch (Exception e) {
            log.error("Experience 조회 실패 experienceId={}", experienceId, e);
            return;
        }
        try {
            Project project = projectService.getById(projectId);
            List<String> keyPoints = experienceSummarizer.generateKeyPoints(
                    project.getTitle(),
                    project.getDescription(),
                    project.getCategory(),
                    project.getSkills(),
                    experience.getContent()
            );
            experience.completeSummarizing(String.join("\n", keyPoints));
        } catch (Exception e) {
            log.error("AI 요약 실패 experienceId={}", experienceId, e);
            experience.failSummarizing();
        }
        experienceService.save(experience);
    }
}
