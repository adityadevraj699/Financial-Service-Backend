package com.Financial.service.exception;

public class JwtAuthException extends RuntimeException {
    
    private final int statusCode;

    public JwtAuthException(String message) {
        super(message);
        this.statusCode = 401;
    }

    public JwtAuthException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}