package com.example.Lipa.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.Lipa.DTOs.BillingDtos;
import com.example.Lipa.entity.PaymentUseCase;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and refunds")
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BILLING_ADMIN') or hasRole('SERVICE_ACCOUNT')")
    @Operation(summary = "Process a payment for an invoice")
    public ResponseEntity<BillingDtos.PaymentResponse> processPayment(
            @Valid @RequestBody BillingDtos.ProcessPaymentRequest request) {

        var payment = paymentUseCase.processPayment(
                new PaymentUseCase.ProcessPaymentCommand(
                        request.customerId(),
                        request.invoiceId(),
                        request.amount(),
                        request.currency(),
                        request.paymentMethodId(),
                        request.idempotencyKey()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BillingDtos.PaymentResponse.from(payment));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a payment by ID")
    public ResponseEntity<BillingDtos.PaymentResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                BillingDtos.PaymentResponse.from(paymentUseCase.getPayment(id))
        );
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('BILLING_ADMIN')")
    @Operation(summary = "Refund a payment (full or partial)")
    public ResponseEntity<BillingDtos.PaymentResponse> refund(
            @PathVariable UUID id,
            @Valid @RequestBody BillingDtos.RefundPaymentRequest request) {

        var payment = paymentUseCase.refundPayment(
                new PaymentUseCase.RefundPaymentCommand(id, request.refundAmount(), request.reason())
        );
        return ResponseEntity.ok(BillingDtos.PaymentResponse.from(payment));
    }
}