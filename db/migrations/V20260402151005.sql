-- experiences 테이블에 ai_summary_status 컬럼 추가
ALTER TABLE experiences
    ADD COLUMN ai_summary_status VARCHAR(20) NOT NULL DEFAULT 'NONE';
