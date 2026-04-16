package kr.ac.kyonggi.common.exception;

import org.springframework.http.HttpStatus;

public class EducationNotFoundException extends BusinessException {

    public EducationNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
