package kr.ac.kyonggi.api;

import kr.ac.kyonggi.domain.experience.ExperienceSummarizer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("test")
public class DummyExperienceSummarizer implements ExperienceSummarizer {

    @Override
    public List<String> generateKeyPoints(String title, String description, String category,
                                          List<String> skills, String content) {
        return List.of("더미 핵심 포인트");
    }

    @Override
    public String summarize(String title, String description, String category,
                             List<String> skills, String content) {
        return "더미 AI 요약: 해당 경험의 핵심 직무 내용을 분석하였습니다.";
    }
}
