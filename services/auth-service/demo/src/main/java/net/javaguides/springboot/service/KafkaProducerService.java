package net.javaguides.springboot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.javaguides.springboot.dto.UserRegistrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;



@Service
public class KafkaProducerService {
    private static final String TOPIC = "user-registration";
    private static final String TOPIC2 = "user-change-event";
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendUserRegistrationEvent(String login, String token) {
        UserRegistrationEvent event = new UserRegistrationEvent();
        event.setLogin(login);
        event.setToken(token);

        try {
            String message = objectMapper.writeValueAsString(event); // Сериализация в JSON
            kafkaTemplate.send(TOPIC, message); // Отправка в Kafka
        } catch (JsonProcessingException e) {
            logger.error("An error occurred", e);
        }
    }

    public void sendUserChangeEvent(String newLogin, String token) {
        UserRegistrationEvent event = new UserRegistrationEvent();
        event.setLogin(newLogin);
        event.setToken(token);

        try {
            String message = objectMapper.writeValueAsString(event); // Сериализация в JSON
            kafkaTemplate.send(TOPIC2, message); // Отправка в Kafka
        } catch (JsonProcessingException e) {
            logger.error("An error occurred", e);
        }
    }
}
