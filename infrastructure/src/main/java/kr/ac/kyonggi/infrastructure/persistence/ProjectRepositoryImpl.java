package kr.ac.kyonggi.infrastructure.persistence;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.ac.kyonggi.domain.project.Project;
import kr.ac.kyonggi.domain.project.ProjectRepositoryCustom;
import kr.ac.kyonggi.domain.project.QProject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QProject project = QProject.project;

    @Override
    public Page<Project> findWithFilters(String category, String keyword, Pageable pageable) {
        List<Project> content = findContentWithFilters(category, keyword, pageable);
        long total = countWithFilters(category, keyword);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<Project> findContentWithFilters(String category, String keyword, Pageable pageable) {
        return queryFactory
                .selectFrom(project)
                .where(categoryEq(category), searchKeyword(keyword))
                .orderBy(project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public long countWithFilters(String category, String keyword) {
        Long total = queryFactory
                .select(project.count())
                .from(project)
                .where(categoryEq(category), searchKeyword(keyword))
                .fetchOne();
        return Objects.requireNonNullElse(total, 0L);
    }

    private BooleanExpression categoryEq(String category) {
        return category != null ? project.category.eq(category) : null;
    }

    private BooleanExpression searchKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return project.title.containsIgnoreCase(keyword)
                .or(project.description.containsIgnoreCase(keyword));
    }
}
