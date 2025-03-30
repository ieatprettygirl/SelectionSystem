package net.javaguides.springboot.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    // Метод для генерации токена
    public String generateToken(String username, Long roleId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role_id", roleId); // Добавляем ID роли в токен

        SecretKey secretKey = new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());

        return Jwts.builder()
                .setClaims(claims) // Устанавливаем данные в токен
                .setIssuedAt(new Date(System.currentTimeMillis())) // Устанавливаем дату выдачи
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // Время жизни токена (неделя)
                .signWith(secretKey) // Подписываем токен
                .compact(); // Создаем токен
    }
    public String extractUsername(String token) {
        return extractClaims(token).getSubject(); // В subject хранится username
    }
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // Передаем ключ
                .build()
                .parseClaimsJws(token) // Разбираем токен
                .getBody(); // Достаем payload (данные)
    }
    private SecretKey getSigningKey() {
        return new SecretKeySpec(jwtSecret.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }
}
