package com.example.Oauth.common.exception;

import com.example.Oauth.auth.exception.ExpiredTokenException;
import com.example.Oauth.auth.exception.InvalidTokenException;
import com.example.Oauth.auth.exception.RevokedTokenException;
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badRequest(IllegalArgumentException e) {
        return build(HttpStatus.BAD_REQUEST, "bad_request", e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<?> invalidToken(InvalidTokenException e) {
        return build(HttpStatus.UNAUTHORIZED, "invalid_token", e.getMessage());
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<?> expiredToken(ExpiredTokenException e) {
        return build(HttpStatus.UNAUTHORIZED, "token_expired", e.getMessage());
    }

    @ExceptionHandler(RevokedTokenException.class)
    public ResponseEntity<?> revokedToken(RevokedTokenException e) {
        return build(HttpStatus.UNAUTHORIZED, "revoked_token", e.getMessage());
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> jwtException(JwtException e) {
        return build(HttpStatus.UNAUTHORIZED, "invalid_jwt", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> internal(Exception e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error", e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", code);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}