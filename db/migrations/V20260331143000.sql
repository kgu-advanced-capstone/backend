-- Date: 2026-03-31
-- 엔티티 연관관계 제거(@ManyToOne → ID 필드)에 따른 스키마 변경
-- - resume_experiences 테이블의 resume_id, project_id 컬럼을 NOT NULL로 변경

ALTER TABLE resume_experiences
    MODIFY COLUMN resume_id BIGINT NOT NULL,
    MODIFY COLUMN project_id BIGINT NOT NULL;
