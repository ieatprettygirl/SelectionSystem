
package net.javaguides.springboot.controller;

import jakarta.validation.Valid;
import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.User;
import net.javaguides.springboot.repository.UserRepository;
import net.javaguides.springboot.service.EmailService;
import net.javaguides.springboot.service.UserService;
import net.javaguides.springboot.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@CrossOrigin(origins = "http://localhost:5432")
@RestController
// base URL
@RequestMapping("/api/")
public class UserController {

    private final EmailService emailService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, JwtUtil jwtUtil, BCryptPasswordEncoder bCryptPasswordEncoder, EmailService emailService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
    }

    // register
    @PostMapping("/auth/register")
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> createUser(@Valid @RequestBody User user) {
        String token = userService.registerUser(user.getLogin(), user.getPassword(), user.getRole());

        emailService.sendVerificationEmail(user.getLogin(), token);

        Map<String, Object> response = new HashMap<>();
        response.put("created", Boolean.TRUE);
        return CompletableFuture.completedFuture(ResponseEntity.ok(response));
    }

    @GetMapping("/auth/confirm")
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> confirmEmail(@RequestParam String token) {
        System.out.println(token);
        Map<String, Object> response = new HashMap<>();
        try {
            String email = jwtUtil.extractUsername(token);

            Optional<User> userOpt = userRepository.findByLogin(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setEnabled(true);
                userRepository.save(user);

                response.put("success", true);
                response.put("message", "User confirmed successfully");
                return CompletableFuture.completedFuture(ResponseEntity.ok(response));
            } else {
                response.put("success", false);
                response.put("message", "Пользователя не существует!");
                return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
        }
    }

    // login and authenticate
    @Async
    @PostMapping("/auth/login")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> login(@RequestBody @Valid Map<String, String> loginRequest) {
        String login = loginRequest.get("login");
        String password = loginRequest.get("password");
        try {
            String token = userService.authenticateUser(login, password);

            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", Boolean.TRUE);
            response.put("token", token);
            return CompletableFuture.completedFuture(ResponseEntity.ok(response));}
        catch (DisabledException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("authenticated", Boolean.FALSE);
            errorResponse.put("error", "Аккаунт не подтвержден! Подтвердите через электронную почту.");
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("authenticated", Boolean.FALSE);
            errorResponse.put("error", e.getMessage());

            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
        }
    }

    // change in your profile (not @Valid else drop)
    @PutMapping("/profile/{id}")
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> updateOwnProfile(@PathVariable Long id, @RequestBody User user) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден!"));

        if (!existingUser.getLogin().equals(currentUsername)) {
            throw new RuntimeException("Вы можете обновить информацию только о своём пользователе!");
        }

        if (user.getLogin() != null && !user.getLogin().equals(existingUser.getLogin())) {
            if (userRepository.existsByLogin(user.getLogin())) {
                throw new RuntimeException("Почта уже зарегестрирована!");
            }
            String newToken = jwtUtil.generateToken(user.getLogin(), existingUser.getRole().getRole_id());
            existingUser.setPendingLogin(user.getLogin());
            emailService.sendConfirmationChangeEmail(existingUser.getPendingLogin(), newToken);
            userRepository.save(existingUser);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Информация о профиле обновлена. Если вы изменили адрес электронной почты, подтвердите его, перейдя по ссылке, отправленной на новый адрес электронной почты.");
            response.put("user", existingUser);

            return CompletableFuture.completedFuture(ResponseEntity.ok(response));
        }

        if (user.getPassword() != null) { existingUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword())); }
        userRepository.save(existingUser);

        String newToken = jwtUtil.generateToken(existingUser.getLogin(), existingUser.getRole().getRole_id());

        Map<String, Object> response = new HashMap<>();
        response.put("user", existingUser);
        response.put("token", newToken);

        return CompletableFuture.completedFuture(ResponseEntity.ok(response));
    }

    @GetMapping("/auth/confirm-email-change")
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> confirmEmailChange(@RequestParam String token) {
        String newEmail = jwtUtil.extractUsername(token);

        Optional<User> userOpt = userRepository.findByPendingLogin(newEmail);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLogin(newEmail);  // Обновляем основной login
            user.setPendingLogin(null);  // Очищаем временное поле
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Почта успешно изменена!");

            return CompletableFuture.completedFuture(ResponseEntity.ok(response));
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка! Невалидный токен.");

            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
        }
    }

    // get information about my user
    @GetMapping("/profile/{id}")
    @Async
    public CompletableFuture<ResponseEntity<User>> getUserById(@PathVariable Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователя не существует!"));

        if (!existingUser.getLogin().equals(currentUsername)) {
            throw new RuntimeException("Вы можете обновлять только информацию о своем профиле!");
        }
        return CompletableFuture.completedFuture(ResponseEntity.ok(existingUser));
    }

    // delete user rest api
    @DeleteMapping("/user/{id}")
    @PreAuthorize("hasRole('ROLE_1')")
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> deleteUser(@PathVariable Long id){
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователя не существует!"));

        if (user.getLogin().equals(currentUsername)) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Вы не можете удалить свой профиль!");
            return CompletableFuture.completedFuture(ResponseEntity.status(403).body(response));
        }

        userRepository.delete(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Пользователь успешно удален!");
        return CompletableFuture.completedFuture(ResponseEntity.ok(response));
    }

    // delete profile
    @DeleteMapping("/profile/{id}")
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> deleteMyProfile(@PathVariable Long id) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователя не существует!"));

        if (!existingUser.getLogin().equals(currentUsername)) {
            throw new RuntimeException("Вы можете удалить только свой профиль!");
        }
        userRepository.delete(existingUser);

        SecurityContextHolder.clearContext();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Профиль успешно удалён!");
        return CompletableFuture.completedFuture(ResponseEntity.ok(response));
    }
}