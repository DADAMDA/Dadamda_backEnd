package com.example.Oauth.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "bad_request");
        body.put("message", e.getMessage()); // null 허용
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> invalidJwt(JwtException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "invalid_token");
        body.put("message", e.getMessage()); // null 허용
        return ResponseEntity.status(401).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> internal(Exception e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", e.getClass().getSimpleName());
        body.put("message", e.getMessage()); // null 허용
        return ResponseEntity.internalServerError().body(body);
    }
}
