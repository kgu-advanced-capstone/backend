package kr.ac.kyonggi.api.resume;

import kr.ac.kyonggi.api.resume.dto.ResumeResponse;
import kr.ac.kyonggi.common.exception.ResumeNotFoundException;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectMember;
import kr.ac.kyonggi.domain.project.ProjectService;
import kr.ac.kyonggi.domain.resume.Resume;
import kr.ac.kyonggi.domain.resume.ResumedExperience;
import kr.ac.kyonggi.domain.resume.ResumeService;
import kr.ac.kyonggi.domain.user.User;
import kr.ac.kyonggi.domain.user.UserService;
import kr.ac.kyonggi.infrastructure.external.GeminiResumeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeApiService {

    private final ResumeService resumeService;
    private final UserService userService;
    private final ProjectService projectService;
    private final GeminiResumeClient geminiClient;

    @Transactional(readOnly = true)
    public ResumeResponse getResume(String email) {
        User user = userService.getByEmail(email);
        Resume resume = resumeService.findByUserId(user.getId())
                .orElseThrow(() -> new ResumeNotFoundException(
                        "이력서가 없습니다. POST /api/resume/generate로 먼저 이력서를 생성해주세요."));
        return ResumeResponse.from(user, resume);
    }

    @Transactional
    public void generate(String email) {
        User user = userService.getByEmail(email);
        Long userId = user.getId();

        List<ProjectMember> memberships = projectService.getMembershipsOf(userId);

        List<ResumedExperience> experiences = memberships.stream()
                .map(member -> {
                    Project project = member.getProject();
                    List<String> keyPoints = geminiClient.generateKeyPoints(
                            project.getTitle(),
                            project.getDescription(),
                            project.getCategory(),
                            project.getSkills()
                    );
                    return ResumedExperience.of(project.getId(), project.getTitle(), keyPoints);
                })
                .toList();

        Resume resume = resumeService.findByUserId(userId)
                .orElseGet(() -> Resume.createFor(userId));
        resume.updateExperiences(experiences);
        resumeService.save(resume);
    }
}
