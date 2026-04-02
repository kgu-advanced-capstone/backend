package kr.ac.kyonggi.common.exception;

import org.springframework.http.HttpStatus;

public class SummarizeAlreadyInProgressException extends BusinessException {

    public SummarizeAlreadyInProgressException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
