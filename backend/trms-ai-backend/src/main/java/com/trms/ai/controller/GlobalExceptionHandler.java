package com.trms.ai.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for TRMS AI Backend
 * Provides consistent error responses and logging
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        logger.warn("Validation error: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(
            "VALIDATION_ERROR",
            "Invalid request parameters",
            errors.toString(),
            ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle HTTP client errors (4xx)
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientError(HttpClientErrorException ex) {
        logger.error("HTTP client error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TRMS_CLIENT_ERROR",
            "Error communicating with TRMS system",
            ex.getMessage(),
            ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Handle HTTP server errors (5xx)
     */
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServerError(HttpServerErrorException ex) {
        logger.error("HTTP server error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TRMS_SERVER_ERROR",
            "TRMS system is experiencing issues",
            ex.getMessage(),
            ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Handle connection errors
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleConnectionError(ResourceAccessException ex) {
        logger.error("Connection error to TRMS system: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            "TRMS_CONNECTION_ERROR",
            "Unable to connect to TRMS system",
            "The TRMS system is currently unavailable. Please try again later.",
            ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Handle general runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime exception: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            ex.getMessage(),
            ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        logger.error("Unexpected exception: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            "UNKNOWN_ERROR",
            "An unexpected system error occurred",
            "Please contact support if this issue persists",
            ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Error response record
     */
    public record ErrorResponse(
        String code,
        String message,
        String details,
        String timestamp
    ) {}
}