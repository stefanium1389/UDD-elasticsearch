package com.example.ddmdemo.security;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestAuthenticationEntryPoint extends ResponseEntityExceptionHandler{
    @ExceptionHandler(AccessDeniedException.class)
    public final ResponseEntity<?> handleAccessDeniedException(Exception ex, WebRequest request) {
        String header = request.getHeader("Authorization");


        if (header == null || !header.startsWith("Bearer ")) {
            return new ResponseEntity<>("Unauthorized!", HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>("Access denied!", HttpStatus.FORBIDDEN);
    }
}
