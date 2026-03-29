package kr.ac.kyonggi.common.exception;

import org.springframework.http.HttpStatus;

public class ExperienceNotFoundException extends BusinessException {

    public ExperienceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
