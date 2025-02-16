package net.javaguides.springboot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import net.javaguides.springboot.model.Vacancy;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CompanyRequest {
    private long company_id;
    private String inn;
    private String kpp;
    private String ogrn;
    private String address;
    private String director;
    private Date date_reg;
    private Boolean is_accepted;
    @JsonIgnoreProperties({"company"})
    private List<Vacancy> vacancies;

    public CompanyRequest(Long company_id, String inn, String kpp, String ogrn, String address, String director, Date date_reg, Boolean is_accepted, List<Vacancy> vacancies) {
        this.company_id = company_id;
        this.inn = inn;
        this.kpp = kpp;
        this.ogrn = ogrn;
        this.address = address;
        this.director = director;
        this.date_reg = date_reg;
        this.is_accepted = is_accepted;
        this.vacancies = vacancies;
    }
}