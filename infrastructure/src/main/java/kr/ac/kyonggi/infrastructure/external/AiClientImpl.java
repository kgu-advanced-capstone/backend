package kr.ac.kyonggi.infrastructure.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ac.kyonggi.common.ai.AiClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiClientImpl implements AiClient {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public AiClientImpl(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String call(String prompt) {
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @Override
    public List<String> extractList(String prompt) {
        String raw = call(prompt);
        int start = raw.indexOf('[');
        int end = raw.lastIndexOf(']');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("AI 응답에서 JSON 배열을 찾을 수 없음: " + raw);
        }
        try {
            return objectMapper.readValue(raw.substring(start, end + 1), new TypeReference<>() {});
        } catch (Exception e) {
            throw new IllegalStateException("AI 응답 파싱 실패: " + e.getMessage(), e);
        }
    }
}
