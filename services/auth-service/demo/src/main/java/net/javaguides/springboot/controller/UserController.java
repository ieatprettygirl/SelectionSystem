package net.javaguides.springboot.controller;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.javaguides.springboot.dto.UserGetOneDTO;
import net.javaguides.springboot.model.User;
import net.javaguides.springboot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@CrossOrigin(origins = "*")
@RestController
@Slf4j
// base URL
@RequestMapping("/api/auth/")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // register user
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> createUser(@Valid @RequestBody User user) throws ExecutionException, InterruptedException, JsonProcessingException {
        return userService.createUser(user).get();
    }

    // confirm account
    @GetMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmEmailUser(@RequestParam String token) throws ExecutionException, InterruptedException {
        return userService.confirmEmailUser(token).get();
    }

    // login and authenticate
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody @Valid Map<String, String> loginRequest) throws ExecutionException, InterruptedException {
        return userService.login(loginRequest).get();
    }

    // confirm change email
    @GetMapping("/confirm-email-change")
    public ResponseEntity<Map<String, Object>> confirmEmailChange(HttpServletRequest request, @RequestParam String token) throws ExecutionException, InterruptedException {
        return userService.confirmEmailChange(token).get();
    }

    // change in your profile (not @Valid else drop)
    @PutMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> updateOwnProfile(HttpServletRequest request, @PathVariable Long id, @RequestBody @Valid UserGetOneDTO userGetOneDTO) throws ExecutionException, InterruptedException {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.updateOwnProfile(request, id, userGetOneDTO, currentUsername).get();
    }

    @PutMapping("/profile/change-login/{id}")
    public ResponseEntity<Map<String, Object>> updateLogin(HttpServletRequest request, @PathVariable Long id, @RequestBody @Valid Map<String, String> loginRequest) throws ExecutionException, InterruptedException {
        return userService.updateLogin(request, id, loginRequest).get();
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>>logout(HttpServletRequest request, HttpServletResponse response) throws ExecutionException, InterruptedException {
        return userService.logout(request, response).get();
    }
}