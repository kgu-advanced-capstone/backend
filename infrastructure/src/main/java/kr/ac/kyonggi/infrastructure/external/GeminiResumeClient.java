package kr.ac.kyonggi.infrastructure.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiResumeClient {

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Value("${gemini.api-key:}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    public List<String> generateKeyPoints(
            String title, String description, String category,
            List<String> skills, String experienceContent) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Gemini API key is not configured. Returning empty key points.");
            return List.of();
        }
        try {
            String requestBody = buildRequestBody(title, description, category, skills, experienceContent);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return parseKeyPoints(response.body());
        } catch (Exception e) {
            log.warn("Gemini API call failed for project '{}': {}", title, e.getMessage());
            return List.of();
        }
    }

    private String buildRequestBody(
            String title, String description, String category,
            List<String> skills, String experienceContent) {
        String prompt = String.format("""
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
                String.join(", ", skills),
                experienceContent != null ? experienceContent : ""
        );
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );
            return objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build Gemini request body", e);
        }
    }

    private List<String> parseKeyPoints(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            int start = text.indexOf('[');
            int end = text.lastIndexOf(']');
            if (start >= 0 && end > start) {
                String jsonArray = text.substring(start, end + 1);
                return objectMapper.readValue(jsonArray, new TypeReference<>() {});
            }
            log.warn("Could not find JSON array in Gemini response: {}", text);
            return List.of();
        } catch (Exception e) {
            log.warn("Failed to parse Gemini response: {}", e.getMessage());
            return List.of();
        }
    }
}
