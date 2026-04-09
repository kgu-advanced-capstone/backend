-- 프로젝트 목록 조회 성능 최적화를 위한 인덱스 추가
-- created_at 단일 인덱스: ORDER BY created_at DESC 커버링 인덱스
-- category + created_at 복합 인덱스: WHERE category = ? ORDER BY created_at DESC 최적화

CREATE INDEX IF NOT EXISTS idx_projects_created_at ON projects (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_projects_category_created_at ON projects (category, created_at DESC);
