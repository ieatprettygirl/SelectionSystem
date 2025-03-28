package net.javaguides.springboot.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private boolean success;
    private String message;

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
