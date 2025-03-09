package net.javaguides.springboot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import net.javaguides.springboot.model.Company;
import net.javaguides.springboot.model.Vacancy;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class CompanyOneDto {
    private long company_id;
    private String name;
    private String inn;
    private String kpp;
    private String ogrn;
    private String address;
    private String director;
    private Date date_reg;
    private Boolean is_accepted;
    @JsonIgnoreProperties({"company"})
    private List<Vacancy> vacancies;

    public CompanyOneDto(String name, Long company_id, String inn, String kpp, String ogrn, String address, String director, Date date_reg, Boolean is_accepted, List<Vacancy> vacancies) {
        this.name = name;
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

    public CompanyOneDto(Company company) {
        this.name = company.getName();
        this.company_id = company.getCompany_id();
        this.inn = company.getInn();
        this.kpp = company.getKpp();
        this.ogrn = company.getOgrn();
        this.address = company.getAddress();
        this.director = company.getDirector();
        this.date_reg = company.getDate_reg();
        this.is_accepted = company.getIs_accepted();
    }

    public CompanyOneDto() {}
}
