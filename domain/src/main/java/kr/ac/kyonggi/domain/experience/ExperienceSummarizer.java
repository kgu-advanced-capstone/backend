package kr.ac.kyonggi.domain.experience;

import java.util.List;

public interface ExperienceSummarizer {

    List<String> generateKeyPoints(String title, String description, String category,
                                   List<String> skills, String content);

    String summarize(String title, String description, String category,
                     List<String> skills, String content);
}
