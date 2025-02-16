package net.javaguides.springboot.dto;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "vacancy")
public class VacancyDto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vacancy_id;

    @NotNull
    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @NotNull
    private Long company_id;

    @NotNull
    @Column(nullable = false)
    private String contact;

    @Column(length = 1000)
    private String experience;

    private String format;

    @Column(length = 1000)
    private String address;

    private String schedule;

    private String hours;

    private Boolean is_educated = false;
}