ALTER TABLE resumes
    ADD COLUMN cover_letter_title VARCHAR(40) NOT NULL DEFAULT '자기소개서' AFTER user_id,
    ADD COLUMN cover_letter_content VARCHAR(4000) NOT NULL DEFAULT '' AFTER cover_letter_title;