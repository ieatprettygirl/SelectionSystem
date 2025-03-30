package net.javaguides.springboot.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private boolean success;
    private String message;

    public ErrorResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
