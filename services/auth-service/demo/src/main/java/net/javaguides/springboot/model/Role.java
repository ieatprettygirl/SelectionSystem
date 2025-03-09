package net.javaguides.springboot.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long role_id;

    @NotNull
    private String title;

    public Role() {}

    public Role(Long role_id, String  title) {
        this.role_id = role_id;
        this.title = title;
    }

    public Role(String  title) {
        this.title = title;
    }

}
