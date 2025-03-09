package net.javaguides.springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "company")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long company_id;

    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    @Column(nullable = false)
    private String inn;

    @NotNull
    @Column(nullable = false)
    private String kpp;

    @NotNull
    @Column(nullable = false)
    private String ogrn;

    @NotNull
    @Column(nullable = false, length = 1000)
    private String address;

    @NotNull
    @Column(nullable = false)
    private String director;

    @NotNull
    @Column(nullable = false)
    private Date date_reg;

    private Boolean is_accepted = false;

    @OneToMany(mappedBy = "company", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.EAGER)
    //@JsonIgnoreProperties({"company"})
    @JsonIgnore
    private List<Vacancy> vacancies;

    public Company() {
    }

    public Company(String name, String inn, String kpp, String ogrn, String address, String director, Date date_reg, Boolean is_accepted) {
        super();
        this.name = name;
        this.inn = inn;
        this.kpp = kpp;
        this.ogrn = ogrn;
        this.address = address;
        this.director = director;
        this.date_reg = date_reg;
        this.is_accepted = is_accepted;
    }
}