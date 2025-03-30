package net.javaguides.springboot.controller;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.springboot.dto.CompanyOneDto;
import net.javaguides.springboot.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Company;
import net.javaguides.springboot.repository.CompanyRepository;

@CrossOrigin(origins = "http://localhost:5432")
@RestController
@EnableAsync
@Slf4j
// base URL
@RequestMapping("/api/comp-vac/")
public class CompanyController {

    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    // get all info about companies
    @PreAuthorize("hasRole('ROLE_1')")
    @GetMapping("/company")
    public List<Company> getAllCompanies() throws ExecutionException, InterruptedException {
        return companyService.getAllCompanies().get();
    }

    // create a company
    @PreAuthorize("hasRole('ROLE_1')")
    @PostMapping("/company")
    public Company createCompany(@RequestBody @Valid Company company) throws ExecutionException, InterruptedException {
        return companyService.createCompany(company).get();
    }

    // get one company
    @GetMapping("/company/{id}")
    public CompanyOneDto getCompanyById(@PathVariable Long id) throws ExecutionException, InterruptedException {
        return companyService.getOneCompany(id).get();
    }

    // update company
    @PreAuthorize("hasRole('ROLE_1')")
    @PutMapping("/company/{id}")
    public Company updateCompany(@PathVariable Long id, @RequestBody @Valid Company companyDetails) throws ExecutionException, InterruptedException {
        return companyService.updateCompany(id, companyDetails).get();
    }

    // delete company
    @PreAuthorize("hasRole('ROLE_1')")
    @DeleteMapping("/company/{id}")
    public Map<String, Object> deleteCompany(@PathVariable Long id) throws ExecutionException, InterruptedException {
        return companyService.deleteCompany(id).get();
    }
}