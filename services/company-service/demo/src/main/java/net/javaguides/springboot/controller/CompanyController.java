package net.javaguides.springboot.controller;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import jakarta.validation.Valid;
import net.javaguides.springboot.dto.CompanyOneDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Company;
import net.javaguides.springboot.repository.CompanyRepository;

@CrossOrigin(origins = "http://localhost:5432")
@RestController
// base URL
@RequestMapping("/api/")
public class CompanyController {

    private final CompanyRepository companyRepository;

    @Autowired
    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // get all info
    @PreAuthorize("hasRole('ROLE_1')")
    @GetMapping("/company")
    @Async
    public CompletableFuture<List<Company>> getAllCompanies(){
        return CompletableFuture.completedFuture(companyRepository.findAll());
    }

    // create
    @PreAuthorize("hasRole('ROLE_1')")
    @PostMapping("/company")
    @Async
    public CompletableFuture<Company> createCompany(@RequestBody @Valid Company company) {
        return CompletableFuture.completedFuture(companyRepository.save(company));
    }

    // get
    @GetMapping("/company/{id}")
    @Async
    public CompletableFuture<ResponseEntity<CompanyOneDto>> getCompanyById(@PathVariable Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Компании с id: " + id + " не существует!"));
        CompanyOneDto companyOneDto = getCompanyDTO(company);
        return CompletableFuture.completedFuture(ResponseEntity.ok(companyOneDto));
    }

    // update
    @PreAuthorize("hasRole('ROLE_1')")
    @PutMapping("/company/{id}")
    @Async
    public CompletableFuture<ResponseEntity<Company>> updateCompany(@PathVariable Long id, @RequestBody @Valid Company companyDetails){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Компании с id: " + id + " не существует!"));

        company.setInn(companyDetails.getInn());
        company.setKpp(companyDetails.getKpp());
        company.setOgrn(companyDetails.getOgrn());
        company.setAddress(companyDetails.getAddress());
        company.setDirector(companyDetails.getDirector());
        company.setDate_reg(companyDetails.getDate_reg());
        company.setIs_accepted(companyDetails.getIs_accepted());

        Company updatedCompany = companyRepository.save(company);
        return CompletableFuture.completedFuture(ResponseEntity.ok(updatedCompany));
    }

    // delete comp rest api
    @PreAuthorize("hasRole('ROLE_1')")
    @DeleteMapping("/company/{id}")
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> deleteCompany(@PathVariable Long id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Компании с id: " + id + " не существует!"));

        companyRepository.delete(company);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Компания успешно удалена!");
        return CompletableFuture.completedFuture(ResponseEntity.ok(response));
    }

    public CompanyOneDto getCompanyDTO(Company company) {
        return new CompanyOneDto(
                company.getCompany_id(),
                company.getInn(),
                company.getKpp(),
                company.getOgrn(),
                company.getAddress(),
                company.getDirector(),
                company.getDate_reg(),
                company.getIs_accepted(),
                company.getVacancies());
    }
}
