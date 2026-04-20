-- (provider_id, provider) 복합 유니크 제약 추가
-- 동시 소셜 로그인 요청으로 인한 중복 사용자 생성 방지

ALTER TABLE users
    ADD CONSTRAINT uq_users_provider_id_provider UNIQUE (provider_id, provider);
