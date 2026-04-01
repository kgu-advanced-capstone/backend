package kr.ac.kyonggi.common.exception;

import org.springframework.http.HttpStatus;

public class ProjectFullException extends BusinessException {

    public ProjectFullException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
