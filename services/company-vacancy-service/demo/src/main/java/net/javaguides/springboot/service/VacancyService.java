package net.javaguides.springboot.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.springboot.dto.VacancyDto;
import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Company;
import net.javaguides.springboot.model.Vacancy;
import net.javaguides.springboot.repository.CompanyRepository;
import net.javaguides.springboot.repository.VacancyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class VacancyService {

    private final VacancyRepository vacancyRepository;
    private final CompanyRepository companyRepository;

    @Autowired
    public VacancyService(VacancyRepository vacancyRepository, CompanyRepository companyRepository) {
        this.vacancyRepository = vacancyRepository;
        this.companyRepository = companyRepository;
    }

    @Async
    public CompletableFuture<Vacancy> createVacancy(VacancyDto vacancyDto) {
        // Находим компанию по companyId
        Company company = companyRepository.findById(vacancyDto.getCompany_id())
                .orElseThrow(() -> new ResourceNotFoundException("Компания не найдена!"));
        Vacancy vacancy = new Vacancy(vacancyDto);
        vacancy.setCompany(company);

        log.info("service create vacancy");
        return CompletableFuture.completedFuture(vacancyRepository.save(vacancy));
    }

    @Async
    public CompletableFuture<List<Vacancy>> getAllVacancies() {
        log.info("service get all vacancies");
        return CompletableFuture.completedFuture(vacancyRepository.findByIsHiddenFalse());
    }

    @Async
    public CompletableFuture<List<Vacancy>> getAllVacanciesByAdmin() {
        log.info("service get all vacancies by admin");
        return CompletableFuture.completedFuture(vacancyRepository.findAll());
    }

    @Async
    public CompletableFuture<Vacancy> getOneVacancy(Long id) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Вакансия не найдена!"));
        log.info("service get one vacancy");
        return CompletableFuture.completedFuture(vacancy);
    }

    @Async
    public CompletableFuture<Vacancy> updateVacancy(Long id, @Valid Vacancy vacancyDetails) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Вакансия не найдена!"));
        log.info("service update vacancy");
        vacancy.setVacancy_id(vacancy.getVacancy_id());
        return CompletableFuture.completedFuture(vacancyRepository.save(vacancyDetails));
    }

    @Async
    public CompletableFuture<Map<String, Object>> deleteVacancy(Long id) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Вакансия не найдена!"));

        vacancyRepository.delete(vacancy);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Вакансия успешно удалена!");
        return CompletableFuture.completedFuture(ResponseEntity.ok(response).getBody());
    }

}
