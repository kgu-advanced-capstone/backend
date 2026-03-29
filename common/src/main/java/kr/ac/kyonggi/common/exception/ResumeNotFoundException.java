package kr.ac.kyonggi.common.exception;

import org.springframework.http.HttpStatus;

public class ResumeNotFoundException extends BusinessException {

    public ResumeNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
