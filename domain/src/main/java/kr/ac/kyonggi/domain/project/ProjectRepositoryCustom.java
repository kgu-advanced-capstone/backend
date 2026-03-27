package kr.ac.kyonggi.domain.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectRepositoryCustom {

    Page<Project> findWithFilters(String category, String keyword, Pageable pageable);
}
