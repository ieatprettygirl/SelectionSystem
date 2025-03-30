package net.javaguides.springboot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import net.javaguides.springboot.dto.VacancyDto;

import java.util.Date;

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
    private String name;

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

    private Boolean isHidden = false; // open

    public Vacancy() {}

    public Vacancy(VacancyDto vacancyDto) {
        super();
        this.name = vacancyDto.getName();
        this.title = vacancyDto.getTitle();
        this.description = vacancyDto.getDescription();
        this.contact = vacancyDto.getContact();
        this.experience = vacancyDto.getExperience();
        this.format = vacancyDto.getFormat();
        this.address = vacancyDto.getAddress();
        this.schedule = vacancyDto.getSchedule();
        this.hours = vacancyDto.getHours();
        this.is_educated = vacancyDto.getIs_educated();
        this.isHidden = vacancyDto.getIsHidden();
    }
}
