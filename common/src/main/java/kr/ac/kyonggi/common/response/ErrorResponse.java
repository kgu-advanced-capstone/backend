package kr.ac.kyonggi.common.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private final String message;
    private final int code;
    private final LocalDateTime timestamp;

    private ErrorResponse(String message, int code) {
        this.message = message;
        this.code = code;
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse of(String message, int code) {
        return new ErrorResponse(message, code);
    }
}
