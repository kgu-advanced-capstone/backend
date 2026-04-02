package kr.ac.kyonggi.api.resume;

import kr.ac.kyonggi.api.resume.dto.ResumeResponse;
import kr.ac.kyonggi.common.exception.ResumeNotFoundException;
import kr.ac.kyonggi.domain.experience.Experience;
import kr.ac.kyonggi.domain.experience.ExperienceCreateCommand;
import kr.ac.kyonggi.domain.experience.ExperienceService;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.project.ProjectStatus;
import kr.ac.kyonggi.domain.resume.Resume;
import kr.ac.kyonggi.domain.resume.ResumedExperience;
import kr.ac.kyonggi.domain.resume.ResumedExperienceRepository;
import kr.ac.kyonggi.domain.resume.ResumeService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.experience.ExperienceSummarizer;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeApiService {

    private final ResumeService resumeService;
    private final UserService userService;
    private final ProjectService projectService;
    private final ExperienceService experienceService;
    private final ResumedExperienceRepository resumedExperienceRepository;
    private final ExperienceSummarizer experienceSummarizer;

    @Transactional(readOnly = true)
    public ResumeResponse getResume(String email) {
        User user = userService.getByEmail(email);
        Resume resume = resumeService.findByUserId(user.getId())
                .orElseThrow(() -> new ResumeNotFoundException(
                        "이력서가 없습니다. POST /api/resume/generate로 먼저 이력서를 생성해주세요."));
        List<ResumedExperience> experiences = resumedExperienceRepository.findByResumeId(resume.getId());
        return ResumeResponse.from(user, resume, experiences);
    }

    @Transactional
    public void generate(String email) {
        User user = userService.getByEmail(email);
        Long userId = user.getId();

        List<ProjectMember> memberships = projectService.getMembershipsOf(userId);

        Resume resume = resumeService.findByUserId(userId)
                .orElseGet(() -> Resume.createFor(userId));
        Resume savedResume = resumeService.save(resume);

        resumedExperienceRepository.deleteByResumeId(savedResume.getId());

        List<Long> projectIds = memberships.stream().map(ProjectMember::getProjectId).toList();
        Map<Long, Project> projectMap = projectService.getAllByIds(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, p -> p));
        Map<Long, Experience> experienceMap = experienceService.findByProjectIdsAndUserId(projectIds, userId);

        List<ResumedExperience> experiences = memberships.stream()
                .filter(member -> projectMap.containsKey(member.getProjectId()))
                .filter(member -> projectMap.get(member.getProjectId()).getStatus() != ProjectStatus.RECRUITING)
                .map(member -> {
                    Project project = projectMap.get(member.getProjectId());
                    Experience experience = experienceMap.get(project.getId());
                    String experienceContent = experience != null ? experience.getContent() : null;

                    List<String> keyPoints;
                    if (experience != null && experience.getAiSummary() != null && !experience.getAiSummary().isBlank()) {
                        keyPoints = List.of(experience.getAiSummary().split("\n"));
                    } else {
                        keyPoints = experienceSummarizer.generateKeyPoints(
                                project.getTitle(),
                                project.getDescription(),
                                project.getCategory(),
                                project.getSkills(),
                                experienceContent
                        );
                        Experience expToUpdate = experience != null ? experience
                                : Experience.create(new ExperienceCreateCommand(userId, project.getId(), ""));
                        expToUpdate.updateAiSummary(String.join("\n", keyPoints));
                        experienceService.save(expToUpdate);
                    }
                    return ResumedExperience.of(savedResume.getId(), project.getId(), project.getTitle(), keyPoints);
                })
                .toList();

        resumedExperienceRepository.saveAll(experiences);
        savedResume.markGenerated();
        resumeService.save(savedResume);
    }
}
