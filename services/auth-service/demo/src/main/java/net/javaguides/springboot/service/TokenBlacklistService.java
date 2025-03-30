package net.javaguides.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // Добавление токена в черный список
    public void addToBlacklist(String token) {
        redisTemplate.opsForValue().set(token, "invalid", Duration.ofMinutes(60*24*7)); // неделя
    }

    // Проверка, находится ли токен в черном списке
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }
}