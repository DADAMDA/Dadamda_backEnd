package com.example.Oauth.auth.exception;

public class RevokedTokenException extends RuntimeException {
    public RevokedTokenException(String message) {
        super(message);
    }
}