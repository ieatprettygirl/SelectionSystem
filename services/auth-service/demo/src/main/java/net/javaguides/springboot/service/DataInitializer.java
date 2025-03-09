package net.javaguides.springboot.service;

import jakarta.annotation.PostConstruct;
import net.javaguides.springboot.model.Role;
import net.javaguides.springboot.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataInitializer {

    private final RoleRepository roleRepository;

    @Autowired
    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {
        if (roleRepository.count() == 0) { // Проверяем, пуста ли таблица
            roleRepository.save(new Role("Администратор"));
            roleRepository.save(new Role("Пользователь"));
            roleRepository.save(new Role("Сотрудник"));
        }
    }
}
