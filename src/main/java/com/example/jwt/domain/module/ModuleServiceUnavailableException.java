package com.example.jwt.domain.module;

public class ModuleServiceUnavailableException extends RuntimeException {
    public ModuleServiceUnavailableException(String message) {
        super(message);
    }
}
