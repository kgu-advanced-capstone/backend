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

    @Async("asyncExecutor")
    @Transactional
    public void run(Long experienceId, Long projectId) {
        Experience experience = experienceService.getById(experienceId);
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
