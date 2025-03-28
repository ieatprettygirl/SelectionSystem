package com.example.demo.dto;

import lombok.Data;

@Data
public class UserRegistrationEvent {
    private String login;
    private String token;
}
