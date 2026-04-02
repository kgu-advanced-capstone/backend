package kr.ac.kyonggi.domain.experience;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExperienceTest {

    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 10L;

    @Test
    @DisplayName("create() 직후 content, userId, projectId가 올바르게 설정된다")
    void create_setsFieldsCorrectly() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(USER_ID, PROJECT_ID, "### 구현 내용\n로그인 기능을 개발했습니다."));

        assertThat(experience.getContent()).isEqualTo("### 구현 내용\n로그인 기능을 개발했습니다.");
        assertThat(experience.getUserId()).isEqualTo(USER_ID);
        assertThat(experience.getProjectId()).isEqualTo(PROJECT_ID);
    }

    @Test
    @DisplayName("create() 직후 aiSummary는 null이다")
    void create_initialAiSummaryIsNull() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(USER_ID, PROJECT_ID, "내용"));

        assertThat(experience.getAiSummary()).isNull();
    }

    @Test
    @DisplayName("updateContent()를 호출하면 content가 변경된다")
    void updateContent_changesContent() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(USER_ID, PROJECT_ID, "기존 내용"));

        experience.updateContent("새로운 내용");

        assertThat(experience.getContent()).isEqualTo("새로운 내용");
    }

    @Test
    @DisplayName("updateAiSummary()를 호출하면 aiSummary가 설정된다")
    void updateAiSummary_setsAiSummary() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(USER_ID, PROJECT_ID, "내용"));

        experience.updateAiSummary("JWT를 활용한 보안 인증 시스템 구축");

        assertThat(experience.getAiSummary()).isEqualTo("JWT를 활용한 보안 인증 시스템 구축");
    }

    @Test
    @DisplayName("create() 직후 aiSummaryStatus는 NONE이다")
    void create_initialAiSummaryStatusIsNone() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(USER_ID, PROJECT_ID, "내용"));

        assertThat(experience.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.NONE);
    }

    @Test
    @DisplayName("startSummarizing()을 호출하면 상태가 IN_PROGRESS로 변경된다")
    void startSummarizing_setsStatusToInProgress() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(USER_ID, PROJECT_ID, "내용"));

        experience.startSummarizing();

        assertThat(experience.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("completeSummarizing()을 호출하면 상태가 COMPLETED로 변경되고 요약이 저장된다")
    void completeSummarizing_setsStatusAndSummary() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(USER_ID, PROJECT_ID, "내용"));
        experience.startSummarizing();

        experience.completeSummarizing("포인트1\n포인트2");

        assertThat(experience.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.COMPLETED);
        assertThat(experience.getAiSummary()).isEqualTo("포인트1\n포인트2");
    }

    @Test
    @DisplayName("failSummarizing()을 호출하면 상태가 FAILED로 변경된다")
    void failSummarizing_setsStatusToFailed() {
        Experience experience = Experience.create(
                new ExperienceCreateCommand(USER_ID, PROJECT_ID, "내용"));
        experience.startSummarizing();

        experience.failSummarizing();

        assertThat(experience.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.FAILED);
    }
}
