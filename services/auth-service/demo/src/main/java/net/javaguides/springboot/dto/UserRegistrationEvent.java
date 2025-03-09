package net.javaguides.springboot.dto;

import lombok.Data;

@Data
public class UserRegistrationEvent {
    private String login;
    private String token;
}
