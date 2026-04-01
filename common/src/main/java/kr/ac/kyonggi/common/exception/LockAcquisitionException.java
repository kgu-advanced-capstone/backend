package kr.ac.kyonggi.common.exception;

import org.springframework.http.HttpStatus;

public class LockAcquisitionException extends BusinessException {

    public LockAcquisitionException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
