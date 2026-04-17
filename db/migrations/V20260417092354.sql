-- resume_experience_skills 테이블 추가
-- ResumedExperience 엔티티에 skills(@ElementCollection) 필드 추가에 따른 스키마 변경

CREATE TABLE resume_experience_skills (
    experience_id BIGINT NOT NULL,
    skill VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
