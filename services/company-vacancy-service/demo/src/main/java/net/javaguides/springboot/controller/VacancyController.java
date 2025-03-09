package net.javaguides.springboot.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.springboot.dto.VacancyDto;
import net.javaguides.springboot.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import net.javaguides.springboot.model.Company;
import net.javaguides.springboot.model.Vacancy;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@CrossOrigin(origins = "*")
@RestController
@EnableAsync
@Slf4j
// base URL
@RequestMapping("/api/")
public class VacancyController {

    private final VacancyService vacancyService;

    @Autowired
    public VacancyController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    // get all info about vacancies
    @GetMapping("/vacancy")
    public List<Vacancy> getAllVacancies() throws ExecutionException, InterruptedException {
        //CompletableFuture<List<Company>> allCompanies = companyService.getAllCompanies();
        return vacancyService.getAllVacancies().get();
    }

    // get all info about vacancies by admin
    @PreAuthorize("hasRole('ROLE_1')")
    @GetMapping("/admin/vacancy")
    public List<Vacancy> getAllVacanciesByAdmin() throws ExecutionException, InterruptedException {
        return vacancyService.getAllVacanciesByAdmin().get();
    }

    // create vacancy
    @PreAuthorize("hasRole('ROLE_3') || hasRole('ROLE_1')")
    @PostMapping("/vacancy")
    public Vacancy createVacancy(@RequestBody @Valid VacancyDto vacancyDto) throws ExecutionException, InterruptedException {
        return vacancyService.createVacancy(vacancyDto).get();
    }

    // get one vacancy
    @GetMapping("/vacancy/{id}")
    public Vacancy getVacancyById(@PathVariable Long id) throws ExecutionException, InterruptedException {
        return vacancyService.getOneVacancy(id).get();
    }

    // update vacancy
    @PreAuthorize("hasRole('ROLE_3') || hasRole('ROLE_1')")
    @PutMapping("/vacancy/{id}")
    public Vacancy updateVacancy(@PathVariable Long id, @RequestBody @Valid Vacancy vacancyDetails) throws ExecutionException, InterruptedException {
        return vacancyService.updateVacancy(id, vacancyDetails).get();
    }

    // delete vacancy
    @PreAuthorize("hasRole('ROLE_3') || hasRole('ROLE_1')")
    @DeleteMapping("/vacancy/{id}")
    public Map<String, Object> deleteVacancy(@PathVariable Long id) throws ExecutionException, InterruptedException {
        return vacancyService.deleteVacancy(id).get();
    }
}
