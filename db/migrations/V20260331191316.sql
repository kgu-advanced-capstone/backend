-- projects 테이블에 current_member_count 컬럼 추가
-- 기존 데이터는 project_members 집계로 초기화

ALTER TABLE projects
    ADD COLUMN current_member_count INT NOT NULL DEFAULT 0;

UPDATE projects p
SET current_member_count = (
    SELECT COUNT(*) FROM project_members pm WHERE pm.project_id = p.id
);
