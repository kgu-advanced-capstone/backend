package kr.ac.kyonggi.domain.project;

import kr.ac.kyonggi.domain.user.User;

import java.time.LocalDate;
import java.util.List;

public record ProjectCreateCommand(
        String title,
        String description,
        String category,
        List<String> skills,
        int maxMembers,
        LocalDate deadline,
        User author
) {

}
