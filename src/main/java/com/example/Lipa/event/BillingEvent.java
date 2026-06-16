package com.example.Lipa.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Sealed hierarchy of domain events published to Kafka.
 * Downstream services (notifications, analytics) consume these.
 */
public sealed interface BillingEvent permits
        BillingEvent.SubscriptionCreated,
        BillingEvent.SubscriptionCanceled,
        BillingEvent.SubscriptionPastDue,
        BillingEvent.PaymentSucceeded,
        BillingEvent.PaymentFailed,
        BillingEvent.InvoiceFinalized,
        BillingEvent.TrialWillEnd {

    UUID eventId();
    Instant occurredAt();
    String type();

    record SubscriptionCreated(
            UUID eventId,
            Instant occurredAt,
            UUID subscriptionId,
            UUID customerId,
            UUID planId,
            String planName,
            boolean hasTrial
    ) implements BillingEvent {
        public SubscriptionCreated(UUID subscriptionId, UUID customerId, UUID planId,
                                   String planName, boolean hasTrial) {
            this(UUID.randomUUID(), Instant.now(), subscriptionId, customerId, planId, planName, hasTrial);
        }
        @Override public String type() { return "SUBSCRIPTION_CREATED"; }
    }

    record SubscriptionCanceled(
            UUID eventId,
            Instant occurredAt,
            UUID subscriptionId,
            UUID customerId,
            boolean immediate
    ) implements BillingEvent {
        public SubscriptionCanceled(UUID subscriptionId, UUID customerId, boolean immediate) {
            this(UUID.randomUUID(), Instant.now(), subscriptionId, customerId, immediate);
        }
        @Override public String type() { return "SUBSCRIPTION_CANCELED"; }
    }

    record SubscriptionPastDue(
            UUID eventId,
            Instant occurredAt,
            UUID subscriptionId,
            UUID customerId
    ) implements BillingEvent {
        public SubscriptionPastDue(UUID subscriptionId, UUID customerId) {
            this(UUID.randomUUID(), Instant.now(), subscriptionId, customerId);
        }
        @Override public String type() { return "SUBSCRIPTION_PAST_DUE"; }
    }

    record PaymentSucceeded(
            UUID eventId,
            Instant occurredAt,
            UUID paymentId,
            UUID customerId,
            UUID invoiceId,
            java.math.BigDecimal amount,
            String currency
    ) implements BillingEvent {
        public PaymentSucceeded(UUID paymentId, UUID customerId, UUID invoiceId,
                                java.math.BigDecimal amount, String currency) {
            this(UUID.randomUUID(), Instant.now(), paymentId, customerId, invoiceId, amount, currency);
        }
        @Override public String type() { return "PAYMENT_SUCCEEDED"; }
    }

    record PaymentFailed(
            UUID eventId,
            Instant occurredAt,
            UUID paymentId,
            UUID customerId,
            UUID invoiceId,
            String failureCode,
            String failureMessage
    ) implements BillingEvent {
        public PaymentFailed(UUID paymentId, UUID customerId, UUID invoiceId,
                             String failureCode, String failureMessage) {
            this(UUID.randomUUID(), Instant.now(), paymentId, customerId, invoiceId, failureCode, failureMessage);
        }
        @Override public String type() { return "PAYMENT_FAILED"; }
    }

    record InvoiceFinalized(
            UUID eventId,
            Instant occurredAt,
            UUID invoiceId,
            UUID customerId,
            String invoiceNumber,
            java.math.BigDecimal total,
            String currency,
            String hostedInvoiceUrl
    ) implements BillingEvent {
        public InvoiceFinalized(UUID invoiceId, UUID customerId, String invoiceNumber,
                                java.math.BigDecimal total, String currency, String hostedInvoiceUrl) {
            this(UUID.randomUUID(), Instant.now(), invoiceId, customerId,
                    invoiceNumber, total, currency, hostedInvoiceUrl);
        }
        @Override public String type() { return "INVOICE_FINALIZED"; }
    }

    record TrialWillEnd(
            UUID eventId,
            Instant occurredAt,
            UUID subscriptionId,
            UUID customerId,
            Instant trialEndDate
    ) implements BillingEvent {
        public TrialWillEnd(UUID subscriptionId, UUID customerId, Instant trialEndDate) {
            this(UUID.randomUUID(), Instant.now(), subscriptionId, customerId, trialEndDate);
        }
        @Override public String type() { return "TRIAL_WILL_END"; }
    }
}