package net.javaguides.springboot.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.springboot.dto.UserGetOneDTO;
import net.javaguides.springboot.exception.ResourceNotFoundException;
import net.javaguides.springboot.model.Role;
import net.javaguides.springboot.model.User;
import net.javaguides.springboot.repository.RoleRepository;
import net.javaguides.springboot.repository.UserRepository;
import net.javaguides.springboot.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserService {

    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil; // Добавьте JwtUtil
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenBlacklistService tokenBlacklistService;
    private final KafkaProducerService kafkaProducerService;

    // Конструктор для инъекции зависимостей
    @Autowired
    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       RoleRepository roleRepository,
                       JwtUtil jwtUtil,
                       BCryptPasswordEncoder bCryptPasswordEncoder,
                       TokenBlacklistService tokenBlacklistService,
                       KafkaProducerService kafkaProducerService,
                       ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;
        this.kafkaProducerService = kafkaProducerService;
        this.objectMapper = objectMapper;
    }

    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> createUser(User user) {
        Map<String, Object> response = new HashMap<>();
        if (isValidEmail(user.getLogin())) {
            response.put("success", false);
            response.put("message", "Невалидный email!");
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
        }

        String token = registerUser(user.getLogin(), user.getPassword(), user.getRole());

        kafkaProducerService.sendUserRegistrationEvent(user.getLogin(), token); // отправка события в кафку

//        emailService.sendVerificationEmail(user.getLogin(), token);
        response.put("created", Boolean.TRUE);
        return CompletableFuture.completedFuture(ResponseEntity.ok(response));
    }

    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> confirmEmailUser(String token) {
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

    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> login(Map<String, String> loginRequest) {
        String login = loginRequest.get("login");
        String password = loginRequest.get("password");
        try {
            String token = authenticateUser(login, password);

            Map<String, Object> response = new HashMap<>();
            response.put("authenticated", Boolean.TRUE);
            response.put("token", token);
            return CompletableFuture.completedFuture(ResponseEntity.ok(response));
        }
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

    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> confirmEmailChange(String token) {
        String newEmail = jwtUtil.extractUsername(token);
        Optional<User> userOpt = userRepository.findByPendingLogin(newEmail);
        if (userOpt.isPresent()) {

            User user = userOpt.get();
            user.setLogin(newEmail);  // Обновляем основной login
            user.setPendingLogin(null);  // Очищаем временное поле
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);

            return CompletableFuture.completedFuture(ResponseEntity.ok(response));
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Ошибка! Невалидный токен.");

            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
        }
    }

    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> updateOwnProfile(HttpServletRequest request, Long id, UserGetOneDTO userGetOneDTO, String currentUsername) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден!"));
        //логика с ролью
        if (userGetOneDTO.getRole_id() != 1) {
            Role role = roleRepository.findById(userGetOneDTO.getRole_id())
                    .orElseThrow(() -> new ResourceNotFoundException("Не удалось установить указанную роль!"));
            existingUser.setRole(role);
        }
        else {throw new ResourceNotFoundException("Недостаточно прав!"); }
        if (!existingUser.getLogin().equals(currentUsername)) {
            throw new ResourceNotFoundException("Нет доступа!");
        }
        Map<String, Object> response = new HashMap<>();
        if (userGetOneDTO.getPassword() != null) { existingUser.setPassword(bCryptPasswordEncoder.encode(userGetOneDTO.getPassword())); }
        userRepository.save(existingUser);
        String newToken = jwtUtil.generateToken(existingUser.getLogin(), existingUser.getRole().getRole_id());

        String token = extractToken(request);
        tokenBlacklistService.addToBlacklist(token);

        response.put("user", existingUser);
        response.put("token", newToken);
        return CompletableFuture.completedFuture(ResponseEntity.ok(response));
    }

    // !
    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> updateLogin(HttpServletRequest request, Long id, Map<String, String> loginRequest) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден!"));
        String login = loginRequest.get("login");
        Map<String, Object> response = new HashMap<>();
        if (isValidEmail(login)) {
            response.put("message", "Невалидный email!");
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
        }
        if (login != null && !login.equals(existingUser.getLogin())) {
            checkLoginUnique(login);

            String newToken = jwtUtil.generateToken(login, existingUser.getRole().getRole_id());
            existingUser.setPendingLogin(login);

            // emailService.sendConfirmationChangeEmail(existingUser.getPendingLogin(), newToken);
            userRepository.save(existingUser);
            kafkaProducerService.sendUserChangeEvent(existingUser.getPendingLogin(), newToken);

            // String token = extractToken(request);
            // tokenBlacklistService.addToBlacklist(token);

            response.put("message", "Если вы изменили адрес электронной почты, подтвердите его, перейдя по ссылке, отправленной на указанный адрес электронной почты.");
        }
        else {response.put("message", "Изменений не было!"); }
        return CompletableFuture.completedFuture(ResponseEntity.ok(response));
    }

    @Async
    public CompletableFuture<ResponseEntity<Map<String, Object>>> logout(HttpServletRequest request, HttpServletResponse response) {
        // Выполняем выход из системы
        String token = extractToken(request);
        tokenBlacklistService.addToBlacklist(token); // add to reddis black list
        Map<String, Object> responses = new HashMap<>();
        responses.put("success", true);
        return CompletableFuture.completedFuture(ResponseEntity.ok(responses));
    }

    // METHODS
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public void checkLoginUnique(String login) {
        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Данная почта уже используется!");
        }
    }

    public String registerUser(String login, String password, Role role) {

        checkLoginUnique(login); // проверка уникальности
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(login, hashedPassword, role);
        user.setEnabled(false);
        user.setPendingLogin(null);
        user.setCompany_id(null);
        Optional<Role> optionalRole = roleRepository.findById(2L);
        if (optionalRole.isPresent()) { user.setRole(optionalRole.get()); }
        else { throw new RuntimeException("Роль не найдена!"); }
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

    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    public boolean isValidEmail(String email) {
        if (email == null) {
            return true;
        }
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return !pattern.matcher(email).matches();
    }
}