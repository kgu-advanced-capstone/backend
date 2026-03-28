package kr.ac.kyonggi.common.exception;

import org.springframework.http.HttpStatus;

public class ProjectNotFoundException extends BusinessException {

    public ProjectNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
