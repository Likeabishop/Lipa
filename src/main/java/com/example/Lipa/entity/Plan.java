package com.example.Lipa.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.example.Lipa.enums.BillingInterval;

import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a pricing plan in the billing domain.
 * Domain model — framework-free, unit-testable in isolation.
 */
@Setter
@NoArgsConstructor
public class Plan {

    private UUID planId;
    private String name;
    private String description;
    private BigDecimal amount;
    private String currency;
    private BillingInterval billingInterval;
    private Integer trialDays;
    private boolean active;
    private Integer maxSeats;
    private String stripeProductId;
    private String stripePriceId;
    private Instant createdAt;
    private Instant updatedAt;

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    public static Plan create(
            String name,
            String description,
            BigDecimal amount,
            String currency,
            BillingInterval billingInterval,
            Integer trialDays,
            Integer maxSeats
    ) {
        validateAmount(amount);
        return new Plan(
                UUID.randomUUID(), name, description, amount,
                currency.toUpperCase(), billingInterval,
                trialDays != null ? trialDays : 0, true, maxSeats,
                null, null, Instant.now(), Instant.now()
        );
    }

    // -------------------------------------------------------------------------
    // Business rules
    // -------------------------------------------------------------------------

    public BigDecimal monthlyEquivalent() {
        return amount.divide(BigDecimal.valueOf(billingInterval.toMonths()),
                2, java.math.RoundingMode.HALF_UP);
    }

    public boolean hasTrial() {
        return trialDays != null && trialDays > 0;
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void attachStripeIds(String stripeProductId, String stripePriceId) {
        this.stripeProductId = stripeProductId;
        this.stripePriceId = stripePriceId;
        this.updatedAt = Instant.now();
    }

    private static void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Plan amount must be greater than zero");
        }
    }

    // -------------------------------------------------------------------------
    // Constructor (private — use factory)
    // -------------------------------------------------------------------------

    private Plan(UUID planId, String name, String description, BigDecimal amount,
                 String currency, BillingInterval billingInterval, Integer trialDays,
                 boolean active, Integer maxSeats, String stripeProductId,
                 String stripePriceId, Instant createdAt, Instant updatedAt) {
        this.planId = planId;
        this.name = name;
        this.description = description;
        this.amount = amount;
        this.currency = currency;
        this.billingInterval = billingInterval;
        this.trialDays = trialDays;
        this.active = active;
        this.maxSeats = maxSeats;
        this.stripeProductId = stripeProductId;
        this.stripePriceId = stripePriceId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // -------------------------------------------------------------------------
    // Getters (immutable-friendly)
    // -------------------------------------------------------------------------

    public UUID getPlanId() { return planId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public BillingInterval getBillingInterval() { return billingInterval; }
    public Integer getTrialDays() { return trialDays; }
    public boolean isActive() { return active; }
    public Integer getMaxSeats() { return maxSeats; }
    public String getStripeProductId() { return stripeProductId; }
    public String getStripePriceId() { return stripePriceId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}