package com.example.Lipa.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.example.Lipa.enums.PaymentStatus;

/**
 * Payment represents a single charge attempt against a PaymentMethod.
 * Tracks full lifecycle from pending → succeeded/failed, and refunds.
 */
public class Payment {

    private final UUID id;
    private final UUID customerId;
    private final UUID invoiceId;
    private PaymentStatus status;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private String currency;
    private String stripePaymentIntentId;
    private String stripeChargeId;
    private String paymentMethodId;
    private String failureCode;
    private String failureMessage;
    private final String idempotencyKey;
    private Instant processedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    public static Payment create(UUID customerId, UUID invoiceId, BigDecimal amount,
                                 String currency, String paymentMethodId) {
        return new Payment(
                UUID.randomUUID(), customerId, invoiceId, PaymentStatus.PENDING,
                amount, BigDecimal.ZERO, currency.toUpperCase(),
                null, null, paymentMethodId, null, null,
                UUID.randomUUID().toString(), null, Instant.now(), Instant.now()
        );
    }

    // -------------------------------------------------------------------------
    // Business rules
    // -------------------------------------------------------------------------

    public void markProcessing(String stripePaymentIntentId) {
        this.status = PaymentStatus.PROCESSING;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.updatedAt = Instant.now();
    }

    public void succeed(String stripeChargeId) {
        this.status = PaymentStatus.SUCCEEDED;
        this.stripeChargeId = stripeChargeId;
        this.processedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void fail(String failureCode, String failureMessage) {
        this.status = PaymentStatus.FAILED;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.updatedAt = Instant.now();
    }

    public void refund(BigDecimal refundAmount) {
        if (status != PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Only succeeded payments can be refunded");
        }
        BigDecimal newRefunded = this.refundedAmount.add(refundAmount);
        if (newRefunded.compareTo(this.amount) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds payment amount");
        }
        this.refundedAmount = newRefunded;
        this.status = newRefunded.compareTo(this.amount) == 0
                ? PaymentStatus.REFUNDED
                : PaymentStatus.PARTIALLY_REFUNDED;
        this.updatedAt = Instant.now();
    }

    public BigDecimal netAmount() {
        return amount.subtract(refundedAmount);
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    private Payment(UUID id, UUID customerId, UUID invoiceId, PaymentStatus status,
                    BigDecimal amount, BigDecimal refundedAmount, String currency,
                    String stripePaymentIntentId, String stripeChargeId,
                    String paymentMethodId, String failureCode, String failureMessage,
                    String idempotencyKey, Instant processedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.invoiceId = invoiceId;
        this.status = status;
        this.amount = amount;
        this.refundedAmount = refundedAmount;
        this.currency = currency;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.stripeChargeId = stripeChargeId;
        this.paymentMethodId = paymentMethodId;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.idempotencyKey = idempotencyKey;
        this.processedAt = processedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getCustomerId() { return customerId; }
    public UUID getInvoiceId() { return invoiceId; }
    public PaymentStatus getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public String getCurrency() { return currency; }
    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public String getStripeChargeId() { return stripeChargeId; }
    public String getPaymentMethodId() { return paymentMethodId; }
    public String getFailureCode() { return failureCode; }
    public String getFailureMessage() { return failureMessage; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getProcessedAt() { return processedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}