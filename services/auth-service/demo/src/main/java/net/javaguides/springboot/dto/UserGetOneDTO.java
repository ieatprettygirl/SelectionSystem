package net.javaguides.springboot.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import net.javaguides.springboot.model.Role;

@Getter
@Setter
public class UserGetOneDTO {

    @Column(nullable = false, unique = true)
    private String login;

    @NotNull
    @Column(nullable = false)
    private String password;

    @NotNull
    private Long role_id;

    private Long company_id;

}