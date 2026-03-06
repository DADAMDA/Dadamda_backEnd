package com.example.Oauth.auth;

import com.example.Oauth.common.exception.MetadataCrawlException;
import com.example.Oauth.common.exception.ResourceNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "validation_error");
        body.put("message", exception.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(message -> message != null && !message.isBlank())
                .distinct()
                .collect(Collectors.joining(", ")));
        ResponseEntity<Map<String, Object>> response = ResponseEntity.badRequest().body(body);
        return response;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "bad_request");
        body.put("message", exception.getMessage());
        ResponseEntity<Map<String, Object>> response = ResponseEntity.badRequest().body(body);
        return response;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "not_found");
        body.put("message", exception.getMessage());
        ResponseEntity<Map<String, Object>> response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        return response;
    }

    @ExceptionHandler(MetadataCrawlException.class)
    public ResponseEntity<Map<String, Object>> crawlFailure(MetadataCrawlException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "metadata_crawl_failed");
        body.put("message", exception.getMessage());
        ResponseEntity<Map<String, Object>> response = ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
        return response;
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> invalidJwt(JwtException exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "invalid_token");
        body.put("message", exception.getMessage());
        ResponseEntity<Map<String, Object>> response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        return response;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> internal(Exception exception) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", exception.getClass().getSimpleName());
        body.put("message", exception.getMessage());
        ResponseEntity<Map<String, Object>> response = ResponseEntity.internalServerError().body(body);
        return response;
    }
}
