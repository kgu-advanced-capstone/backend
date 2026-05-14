-- Date: 2026-04-01
-- 소셜 로그인 지원을 위한 users 테이블 컬럼 추가
-- provider_id: 소셜 서비스 고유 사용자 ID (Google sub 등)
-- provider: 가입 경로 구분 (LOCAL, GOOGLE, KAKAO, NAVER)
-- role: 사용자 권한 (USER, ADMIN)
-- password: 소셜 로그인 사용자는 비밀번호가 없으므로 nullable로 변경

ALTER TABLE users
    MODIFY COLUMN password VARCHAR(255) NULL,
    ADD COLUMN provider_id VARCHAR(255),
    ADD COLUMN provider    VARCHAR(20) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN role        VARCHAR(20) NOT NULL DEFAULT 'USER';