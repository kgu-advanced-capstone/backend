package kr.ac.kyonggi.common.exception;

import org.springframework.http.HttpStatus;

public class ProjectNotRecruitingException extends BusinessException {

    public ProjectNotRecruitingException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
