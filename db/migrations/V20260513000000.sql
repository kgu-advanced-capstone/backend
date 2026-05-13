-- Date: 2026-05-13 00:00:00
-- 소셜 로그인 사용자는 password가 없으므로 NULL 허용으로 변경

ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL;
