package net.javaguides.springboot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.javaguides.springboot.model.Company;

public interface CompanyRepository extends JpaRepository<Company, Long>{
}
