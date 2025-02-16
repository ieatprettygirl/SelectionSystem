package net.javaguides.springboot.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import net.javaguides.springboot.model.Role;
import net.javaguides.springboot.model.User;
import net.javaguides.springboot.repository.RoleRepository;
import net.javaguides.springboot.repository.UserRepository;
import net.javaguides.springboot.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil; // Добавьте JwtUtil
    private final EmailService emailService;

    // Конструктор для инъекции зависимостей
    @Autowired
    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       RoleRepository roleRepository,
                       JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }


    public String registerUser(String login, String password, Role role) {

        if (userRepository.existsByLogin(login)) {
            throw new RuntimeException("A user with this email already exists!");
        }

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(login, hashedPassword, role);
        user.setEnabled(false);

        Optional<Role> optionalRole = roleRepository.findById(2L);

        if (optionalRole.isPresent()) {
            user.setRole(optionalRole.get());
        } else {
            throw new RuntimeException("Role not found");
        }

        userRepository.save(user);

        return jwtUtil.generateToken(user.getLogin(), user.getRole().getRole_id());
    }

    public String authenticateUser(String login, String password) {
        User user = userRepository.findByLogin(login)
               .orElseThrow(() -> new RuntimeException("Неверный логин или пароль!"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Неверный логин или пароль!");
        }

        if (!user.isEnabled()) {
            throw new DisabledException("Аккаунт не подтвержден. Проверьте почту.");
        }

        return jwtUtil.generateToken(user.getLogin(), user.getRole().getRole_id());
    }
}