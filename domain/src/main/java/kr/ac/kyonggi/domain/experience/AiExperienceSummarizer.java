package kr.ac.kyonggi.domain.experience;

import kr.ac.kyonggi.common.ai.AiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class AiExperienceSummarizer implements ExperienceSummarizer {

    private final AiClient aiClient;

    @Override
    public List<String> generateKeyPoints(String title, String description, String category,
                                          List<String> skills, String content) {
        return aiClient.extractList(buildPrompt(title, description, category, skills, content));
    }

    @Override
    public String summarize(String title, String description, String category,
                             List<String> skills, String content) {
        return String.join("\n", generateKeyPoints(title, description, category, skills, content));
    }

    private String buildPrompt(String title, String description, String category,
                                List<String> skills, String content) {
        return String.format("""
                다음 프로젝트 정보와 참여자의 경험 기록을 바탕으로, 이력서에 기재할 핵심 성과 포인트 2~3개를 한국어로 작성해주세요.
                프로젝트명: %s
                설명: %s
                카테고리: %s
                기술 스택: %s
                참여자 경험 기록: %s

                응답은 반드시 JSON 배열 형식으로만 작성하세요. 예시: ["포인트1", "포인트2", "포인트3"]
                """,
                title,
                description != null ? description : "",
                category,
                skills != null ? String.join(", ", skills) : "",
                content != null ? content : ""
        );
    }
}
