package net.javaguides.springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "vacancy")
public class Vacancy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long vacancy_id;

    @NotNull
    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

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