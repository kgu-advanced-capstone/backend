
package kr.ac.kyonggi.domain.resume;

import java.util.List;

public interface ResumeAiClient {
    List<String> generateKeyPoints(String title, String description, String category,
                                   List<String> skills, String experienceContent);
}
