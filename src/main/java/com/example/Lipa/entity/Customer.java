package com.example.Lipa.entity;

import java.time.Instant;
import java.util.UUID;

/**
 * A Customer is the billing identity for a tenant/user.
 * Corresponds to a Stripe Customer object externally.
 */
public class Customer {

    private final UUID customerId;
    private final String externalUserId;   // ID from Auth service / JWT sub
    private String email;
    private String name;
    private String stripeCustomerId;
    private String defaultPaymentMethodId;
    private String currency;
    private boolean taxExempt;
    private final Instant createdAt;
    private Instant updatedAt;

    public static Customer create(String externalUserId, String email, String name, String currency) {
        return new Customer(
                UUID.randomUUID(), externalUserId, email, name,
                null, null, currency.toUpperCase(),
                false, Instant.now(), Instant.now()
        );
    }

    // -------------------------------------------------------------------------
    // Business rules
    // -------------------------------------------------------------------------

    public void attachStripeCustomer(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
        this.updatedAt = Instant.now();
    }

    public void setDefaultPaymentMethod(String paymentMethodId) {
        this.defaultPaymentMethodId = paymentMethodId;
        this.updatedAt = Instant.now();
    }

    public void updateEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public boolean hasStripeAccount() {
        return stripeCustomerId != null && !stripeCustomerId.isBlank();
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    private Customer(UUID customerId, String externalUserId, String email, String name,
                     String stripeCustomerId, String defaultPaymentMethodId,
                     String currency, boolean taxExempt, Instant createdAt, Instant updatedAt) {
        this.customerId = customerId;
        this.externalUserId = externalUserId;
        this.email = email;
        this.name = name;
        this.stripeCustomerId = stripeCustomerId;
        this.defaultPaymentMethodId = defaultPaymentMethodId;
        this.currency = currency;
        this.taxExempt = taxExempt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getCustomerId() { return customerId; }
    public String getExternalUserId() { return externalUserId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getStripeCustomerId() { return stripeCustomerId; }
    public String getDefaultPaymentMethodId() { return defaultPaymentMethodId; }
    public String getCurrency() { return currency; }
    public boolean isTaxExempt() { return taxExempt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static Customer reconstruct(
            UUID customerId,
            String externalUserId,
            String email,
            String name,
            String stripeCustomerId,
            String defaultPaymentMethodId,
            String currency,
            boolean taxExempt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new Customer(
                customerId,
                externalUserId,
                email,
                name,
                stripeCustomerId,
                defaultPaymentMethodId,
                currency,
                taxExempt,
                createdAt,
                updatedAt
        );
    }
}