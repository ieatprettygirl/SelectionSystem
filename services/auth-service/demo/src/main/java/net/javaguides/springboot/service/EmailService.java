package net.javaguides.springboot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String subject = "Подтверждение регистрации";
        String confirmationUrl = "http://localhost:8080/api/auth/confirm?token=" + token;
        String message = "Перейдите по ссылке для подтверждения аккаунта: " + confirmationUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(message);

        mailSender.send(email);
    }

    @Async
    public void sendConfirmationChangeEmail(String to, String token) {
        String subject = "Подтверждение изменения ";
        String confirmationUrl = "http://localhost:8080/api/auth/confirm-email-change?token=" + token;
        String message = "Перейдите по ссылке для смены email: " + confirmationUrl;

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(to);
        email.setSubject(subject);
        email.setText(message);

        mailSender.send(email);
    }
}