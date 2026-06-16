package com.example.Lipa.entity;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentUseCase {

    record ProcessPaymentCommand(
            UUID customerId,
            UUID invoiceId,
            BigDecimal amount,
            String currency,
            String paymentMethodId,
            String idempotencyKey
    ) {}

    record RefundPaymentCommand(
            UUID paymentId,
            BigDecimal refundAmount,
            String reason
    ) {}

    Payment processPayment(ProcessPaymentCommand command);

    Payment refundPayment(RefundPaymentCommand command);

    Payment getPayment(UUID paymentId);
}