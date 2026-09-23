package com.shahbytes.chathub.api;

import com.shahbytes.chathub.api.dto.ApiError;
import com.shahbytes.chathub.api.dto.FieldViolation;
import com.shahbytes.chathub.exception.ConflictException;
import com.shahbytes.chathub.exception.ForbiddenException;
import com.shahbytes.chathub.exception.NotFoundException;
import com.shahbytes.chathub.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> handleNotFoundException(
            NotFoundException e, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage(), request);
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    ResponseEntity<ApiError> handleForbiddenException(
            RuntimeException e, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN", e.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> handleConflictException(
            ConflictException e, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "CONFLICT", e.getMessage(), request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    ResponseEntity<ApiError> handleRateLimitExceededException(
            RateLimitExceededException e, HttpServletRequest request
    ) {
        return error(
                HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e, HttpServletRequest request
    ) {
        var violations = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new FieldViolation(
                        error.getField(), error.getDefaultMessage()))
                .toList();
        var body = new ApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_FAILED",
                "Request validation failed",
                request.getRequestURI(),
                violations
        );
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleException(Exception e, HttpServletRequest request) {
        LOGGER.error(e.getMessage(), e);
        return error(
                HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred", request);
    }

    private ResponseEntity<ApiError> error(
            HttpStatus status, String code, String message,
            HttpServletRequest request
    ) {
        var body = new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                List.of()
        );
        return new ResponseEntity<>(body, status);
    }
}
