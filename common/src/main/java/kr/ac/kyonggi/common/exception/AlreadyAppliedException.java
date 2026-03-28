package kr.ac.kyonggi.common.exception;

import org.springframework.http.HttpStatus;

public class AlreadyAppliedException extends BusinessException {

    public AlreadyAppliedException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
