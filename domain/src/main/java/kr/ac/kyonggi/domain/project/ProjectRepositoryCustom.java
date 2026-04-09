package kr.ac.kyonggi.domain.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectRepositoryCustom {

    Page<Project> findWithFilters(String category, String keyword, Pageable pageable);

    List<Project> findContentWithFilters(String category, String keyword, Pageable pageable);

    long countWithFilters(String category, String keyword);
}
