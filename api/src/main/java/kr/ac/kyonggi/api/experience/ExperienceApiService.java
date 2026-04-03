package kr.ac.kyonggi.api.experience;

import kr.ac.kyonggi.api.experience.dto.AiSummaryStatusResponse;
import kr.ac.kyonggi.api.experience.dto.ExperienceRequest;
import kr.ac.kyonggi.api.experience.dto.ExperienceResponse;
import kr.ac.kyonggi.common.exception.ForbiddenException;
import kr.ac.kyonggi.common.exception.SummarizeAlreadyInProgressException;
import kr.ac.kyonggi.domain.experience.Experience;
import kr.ac.kyonggi.domain.experience.ExperienceCreateCommand;
import kr.ac.kyonggi.domain.experience.ExperienceService;
import kr.ac.kyonggi.domain.project.ProjectMemberRepository;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExperienceApiService {

    private final ExperienceService experienceService;
    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectMemberRepository projectMemberRepository;
    private final ExperienceSummarizeTask experienceSummarizeTask;

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
                .orElseGet(() -> Experience.create(
                        new ExperienceCreateCommand(user.getId(), projectId, request.content())));

        experience.updateContent(request.content());

        return ExperienceResponse.from(experienceService.save(experience));
    }

    @Transactional
    public AiSummaryStatusResponse startSummarize(Long id, String email) {
        User user = userService.getByEmail(email);
        Experience experience = experienceService.getByIdWithLock(id);

        if (!experience.getUserId().equals(user.getId())) {
            throw new ForbiddenException("본인의 경험 기록만 요약할 수 있습니다.");
        }
        if (experience.isSummarizing()) {
            throw new SummarizeAlreadyInProgressException("이미 요약이 진행 중입니다.");
        }

        experience.startSummarizing();
        experienceService.save(experience);

        Long experienceId = experience.getId();
        Long projectId = experience.getProjectId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                experienceSummarizeTask.run(experienceId, projectId);
            }
        });

        return AiSummaryStatusResponse.from(experience);
    }

    public AiSummaryStatusResponse getSummaryStatus(Long id, String email) {
        User user = userService.getByEmail(email);
        Experience experience = experienceService.getById(id);

        if (!experience.getUserId().equals(user.getId())) {
            throw new ForbiddenException("본인의 경험 기록만 조회할 수 있습니다.");
        }

        return AiSummaryStatusResponse.from(experience);
    }

    private void verifyMembership(Long projectId, Long userId) {
        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ForbiddenException("해당 프로젝트의 멤버만 접근할 수 있습니다.");
        }
    }
}
