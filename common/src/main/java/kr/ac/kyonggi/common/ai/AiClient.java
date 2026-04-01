package kr.ac.kyonggi.common.ai;

import java.util.List;

public interface AiClient {

    String call(String prompt);

    List<String> extractList(String prompt);
}
