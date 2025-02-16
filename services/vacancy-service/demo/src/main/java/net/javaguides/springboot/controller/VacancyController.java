package net.javaguides.springboot.controller;

import jakarta.validation.Valid;
import net.javaguides.springboot.dto.VacancyDto;
import net.javaguides.springboot.model.Vacancy;
import net.javaguides.springboot.repository.CompanyRepository;
import net.javaguides.springboot.repository.VacancyRepository;
import net.javaguides.springboot.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@CrossOrigin(origins = "http://localhost:5432")
@RestController
// base URL
@RequestMapping("/api/")
public class VacancyController {

    private final VacancyService vacancyService;
    private final VacancyRepository vacancyRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public VacancyController(VacancyRepository vacancyRepository, VacancyService vacancyService, CompanyRepository companyRepository) {
        this.vacancyRepository = vacancyRepository;
        this.vacancyService = vacancyService;
        this.companyRepository = companyRepository;
    }

    // create
    @PreAuthorize("hasRole('ROLE_3') || hasRole('ROLE_1')")
    @PostMapping("/vacancy")
    @Async
    public CompletableFuture<ResponseEntity<Vacancy>> createVacancy(@RequestBody @Valid VacancyDto vacancyDto) {
        Vacancy vacancy = vacancyService.createVacancy2(vacancyDto);
        return CompletableFuture.completedFuture(ResponseEntity.ok(vacancy));
    }
}
