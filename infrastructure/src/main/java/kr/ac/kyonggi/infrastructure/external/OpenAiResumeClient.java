package kr.ac.kyonggi.infrastructure.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.kyonggi.domain.resume.ResumeAiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class OpenAiResumeClient implements ResumeAiClient {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public OpenAiResumeClient(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> generateKeyPoints(String title, String description, String category,
                                          List<String> skills, String experienceContent) {
        try {
            String prompt = buildPrompt(title, description, category, skills, experienceContent);
            String content = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            return parseKeyPoints(title, content);
        } catch (Exception e) {
            log.warn("OpenAI API 호출 실패 (프로젝트: '{}'): {}", title, e.getMessage());
            return List.of();
        }
    }

    private String buildPrompt(String title, String description, String category,
                                List<String> skills, String experienceContent) {
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
                experienceContent != null ? experienceContent : ""
        );
    }

    private List<String> parseKeyPoints(String title, String content) {
        try {
            int start = content.indexOf('[');
            int end = content.lastIndexOf(']');
            if (start >= 0 && end > start) {
                String jsonArray = content.substring(start, end + 1);
                return objectMapper.readValue(jsonArray, new TypeReference<>() {});
            }
            log.warn("OpenAI 응답에서 JSON 배열을 찾을 수 없음 (프로젝트: '{}'): {}", title, content);
            return List.of();
        } catch (Exception e) {
            log.warn("OpenAI 응답 파싱 실패 (프로젝트: '{}'): {}", title, e.getMessage());
            return List.of();
        }
    }
}
