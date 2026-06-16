package com.example.Lipa.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.Lipa.DTOs.BillingDtos;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<BillingDtos.ApiError> handleNotFound(SubscriptionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BillingDtos.ApiError.of(404, "NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(SubscriptionStateException.class)
    public ResponseEntity<BillingDtos.ApiError> handleStateConflict(SubscriptionStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(BillingDtos.ApiError.of(409, "SUBSCRIPTION_STATE_CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<BillingDtos.ApiError> handlePaymentFailed(PaymentFailedException ex) {
        log.warn("Payment failed with code={}: {}", ex.getStripeCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(BillingDtos.ApiError.of(402, "PAYMENT_FAILED",
                        ex.getMessage() + " [" + ex.getStripeCode() + "]"));
    }

    @ExceptionHandler(BillingException.class)
    public ResponseEntity<BillingDtos.ApiError> handleBilling(BillingException ex) {
        log.error("Billing error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(BillingDtos.ApiError.of(422, "BILLING_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BillingDtos.ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(BillingDtos.ApiError.of(400, "VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BillingDtos.ApiError> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BillingDtos.ApiError.of(500, "INTERNAL_ERROR",
                        "An unexpected error occurred. Please try again."));
    }
}