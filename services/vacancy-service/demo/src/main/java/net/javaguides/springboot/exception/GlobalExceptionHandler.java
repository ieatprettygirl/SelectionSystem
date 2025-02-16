package net.javaguides.springboot.exception;

import lombok.Getter;
import net.javaguides.springboot.model.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Getter
    public static class CompanyNotApprovedException extends RuntimeException {
        private final HttpStatus status;

        public CompanyNotApprovedException(String message, HttpStatus status) {
            super(message);
            this.status = status;
        }

    }

    // Обработка ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Обработка IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AuthResponse> handleRuntimeException(RuntimeException ex) {
        // Формируем ответ с ошибкой
        AuthResponse response = new AuthResponse(false, ex.getMessage());
        // Возвращаем статус 400 (BAD_REQUEST) с сообщением
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<AuthResponse> handleAccessDeniedException(AccessDeniedException ex) {
        AuthResponse response = new AuthResponse(false, "no access");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    //for web-client error
    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ErrorResponse> handleWebClientRequestException(WebClientRequestException ex) {
        // Создаем объект ошибки с нужными данными
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                "Ошибка соеденения с внешним сервисом!"
        );

        // Возвращаем объект в формате JSON с кодом ошибки 500
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errorMessage.append(error.getField())
                        .append(": ")
                        .append(error.getDefaultMessage())
                        .append("; ")
        );

        ErrorResponse errorResponse = new ErrorResponse(false, "Fill in all required fields!");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestBody(HttpMessageNotReadableException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Request body is missing");
        response.put("message", "The request body must be provided in JSON format.");

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CompanyNotApprovedException.class)
    public ResponseEntity<Map<String, Object>> handleCompanyNotApprovedException(CompanyNotApprovedException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

}
