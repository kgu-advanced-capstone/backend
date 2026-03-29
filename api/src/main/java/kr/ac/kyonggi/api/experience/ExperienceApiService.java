package kr.ac.kyonggi.api.experience;

import kr.ac.kyonggi.api.experience.dto.AiSummaryResponse;
import kr.ac.kyonggi.api.experience.dto.ExperienceRequest;
import kr.ac.kyonggi.api.experience.dto.ExperienceResponse;
import kr.ac.kyonggi.common.exception.ForbiddenException;
import kr.ac.kyonggi.domain.experience.Experience;
import kr.ac.kyonggi.domain.experience.ExperienceCreateCommand;
import kr.ac.kyonggi.domain.experience.ExperienceService;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectMemberRepository;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import kr.ac.kyonggi.infrastructure.external.ExperienceSummarizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExperienceApiService {

    private final ExperienceService experienceService;
    private final UserService userService;
    private final ProjectService projectService;
    private final ProjectMemberRepository projectMemberRepository;
    private final ExperienceSummarizer experienceSummarizer;

    public List<ExperienceResponse> getByProject(Long projectId, String email) {
        User user = userService.getByEmail(email);
        verifyMembership(projectId, user.getId());
        return experienceService.getByProjectIdAndUserId(projectId, user.getId())
                .stream()
                .map(ExperienceResponse::from)
                .toList();
    }

    @Transactional
    public ExperienceResponse upsert(Long projectId, ExperienceRequest request, String email) {
        User user = userService.getByEmail(email);
        verifyMembership(projectId, user.getId());

        Experience experience = experienceService
                .findByProjectIdAndUserId(projectId, user.getId())
                .orElseGet(() -> {
                    Project project = projectService.getById(projectId);
                    return Experience.create(new ExperienceCreateCommand(user, project, request.content()));
                });

        experience.updateContent(request.content());

        return ExperienceResponse.from(experienceService.save(experience));
    }

    @Transactional
    public AiSummaryResponse summarize(Long id, String email) {
        User user = userService.getByEmail(email);
        Experience experience = experienceService.getById(id);

        if (!experience.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("본인의 경험 기록만 요약할 수 있습니다.");
        }

        String summary = experienceSummarizer.summarize(experience.getContent());
        experience.updateAiSummary(summary);
        experienceService.save(experience);

        return AiSummaryResponse.from(experience);
    }

    private void verifyMembership(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ForbiddenException("해당 프로젝트의 멤버만 접근할 수 있습니다.");
        }
    }
}
