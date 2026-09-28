package com.silvano.gastos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> erroValidacao(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(e -> e.getDefaultMessage()).orElse("Dados inválidos");
        return ResponseEntity.badRequest().body(Map.of("erro", msg));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> erroStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("erro", String.valueOf(ex.getReason())));
    }
}
