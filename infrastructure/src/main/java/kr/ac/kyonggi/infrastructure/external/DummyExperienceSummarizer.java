package kr.ac.kyonggi.infrastructure.external;

import org.springframework.stereotype.Component;

@Component
public class DummyExperienceSummarizer implements ExperienceSummarizer {

    @Override
    public String summarize(String content) {
        return "더미 AI 요약: 해당 경험의 핵심 직무 내용을 분석하였습니다.";
    }
}
