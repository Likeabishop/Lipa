package com.example.Lipa.DTOs;

import com.example.Lipa.entity.Invoice;
import com.example.Lipa.entity.Payment;
import com.example.Lipa.entity.Plan;
import com.example.Lipa.entity.Subscription;
import com.example.Lipa.enums.BillingInterval;
import com.example.Lipa.enums.InvoiceStatus;
import com.example.Lipa.enums.PaymentStatus;
import com.example.Lipa.enums.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * REST DTO definitions — kept in one file for compact browsability.
 * Each record is its own logical DTO.
 */
public final class BillingDtos {

    private BillingDtos() {}

    // =========================================================================
    // Plan DTOs
    // =========================================================================

    @Schema(description = "Request to create a new pricing plan")
    public record CreatePlanRequest(
            @NotBlank @Size(max = 100) @Schema(example = "Pro") String name,
            @Schema(example = "Unlimited projects and team members") String description,
            @NotNull @DecimalMin("0.01") @Schema(example = "299.00") BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) @Schema(example = "ZAR") String currency,
            @NotNull BillingInterval billingInterval,
            @Min(0) @Max(90) @Schema(example = "14") Integer trialDays,
            @Min(1) @Schema(example = "50") Integer maxSeats
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PlanResponse(
            UUID id,
            String name,
            String description,
            BigDecimal amount,
            String currency,
            BillingInterval billingInterval,
            Integer trialDays,
            boolean active,
            Integer maxSeats,
            BigDecimal monthlyEquivalent,
            Instant createdAt
    ) {
        public static PlanResponse from(Plan plan) {
            return new PlanResponse(
                    plan.getPlanId(), plan.getName(), plan.getDescription(),
                    plan.getAmount(), plan.getCurrency(), plan.getBillingInterval(),
                    plan.getTrialDays(), plan.isActive(), plan.getMaxSeats(),
                    plan.monthlyEquivalent(), plan.getCreatedAt()
            );
        }
    }

    // =========================================================================
    // Subscription DTOs
    // =========================================================================

    public record CreateSubscriptionRequest(
            @NotNull UUID customerId,
            @NotNull UUID planId,
            @NotBlank String paymentMethodId,
            @Min(1) Integer seats,
            String couponCode
    ) {}

    public record CancelSubscriptionRequest(
            boolean immediate
    ) {}

    public record UpgradeSubscriptionRequest(
            @NotNull UUID newPlanId,
            boolean prorated
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SubscriptionResponse(
            UUID id,
            UUID customerId,
            UUID planId,
            SubscriptionStatus status,
            Instant currentPeriodStart,
            Instant currentPeriodEnd,
            Instant trialEnd,
            Instant canceledAt,
            Instant cancelAtPeriodEnd,
            Integer seats,
            boolean scheduledForCancellation,
            boolean inTrial,
            Instant createdAt
    ) {
        public static SubscriptionResponse from(Subscription s) {
            return new SubscriptionResponse(
                    s.getSubscriptionId(), s.getCustomerId(), s.getPlanId(), s.getStatus(),
                    s.getCurrentPeriodStart(), s.getCurrentPeriodEnd(), s.getTrialEnd(),
                    s.getCanceledAt(), s.getCancelAtPeriodEnd(), s.getSeats(),
                    s.isScheduledForCancellation(), s.isInTrial(), s.getCreatedAt()
            );
        }
    }

    // =========================================================================
    // Invoice DTOs
    // =========================================================================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record InvoiceResponse(
            UUID id,
            UUID customerId,
            UUID subscriptionId,
            InvoiceStatus status,
            String invoiceNumber,
            BigDecimal subtotal,
            BigDecimal taxAmount,
            BigDecimal discountAmount,
            BigDecimal total,
            String currency,
            Instant dueDate,
            Instant paidAt,
            String hostedInvoiceUrl,
            List<LineItemResponse> lineItems,
            Instant createdAt
    ) {
        public record LineItemResponse(String description, BigDecimal unitAmount,
                                       int quantity, BigDecimal lineTotal) {}

        public static InvoiceResponse from(Invoice inv) {
            return new InvoiceResponse(
                    inv.getId(), inv.getCustomerId(), inv.getSubscriptionId(),
                    inv.getStatus(), inv.getInvoiceNumber(),
                    inv.getSubtotal(), inv.getTaxAmount(), inv.getDiscountAmount(),
                    inv.getTotal(), inv.getCurrency(), inv.getDueDate(),
                    inv.getPaidAt(), inv.getHostedInvoiceUrl(),
                    inv.getLineItems().stream().map(li -> new LineItemResponse(
                            li.description(), li.unitAmount(), li.quantity(), li.lineTotal()
                    )).toList(),
                    inv.getCreatedAt()
            );
        }
    }

    // =========================================================================
    // Payment DTOs
    // =========================================================================

    public record ProcessPaymentRequest(
            @NotNull UUID customerId,
            @NotNull UUID invoiceId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) String currency,
            @NotBlank String paymentMethodId,
            String idempotencyKey
    ) {}

    public record RefundPaymentRequest(
            @NotNull @DecimalMin("0.01") BigDecimal refundAmount,
            String reason
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PaymentResponse(
            UUID id,
            UUID customerId,
            UUID invoiceId,
            PaymentStatus status,
            BigDecimal amount,
            BigDecimal refundedAmount,
            BigDecimal netAmount,
            String currency,
            String stripePaymentIntentId,
            String failureCode,
            String failureMessage,
            Instant processedAt,
            Instant createdAt
    ) {
        public static PaymentResponse from(Payment p) {
            return new PaymentResponse(
                    p.getPaymentId(), p.getCustomerId(), p.getInvoiceId(), p.getStatus(),
                    p.getAmount(), p.getRefundedAmount(), p.netAmount(),
                    p.getCurrency(), p.getStripePaymentIntentId(),
                    p.getFailureCode(), p.getFailureMessage(),
                    p.getProcessedAt(), p.getCreatedAt()
            );
        }
    }

    // =========================================================================
    // Common
    // =========================================================================

    public record ApiError(
            int status,
            String error,
            String message,
            Instant timestamp
    ) {
        public static ApiError of(int status, String error, String message) {
            return new ApiError(status, error, message, Instant.now());
        }
    }
}