package com.example.Lipa.entity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.example.Lipa.enums.SubscriptionStatus;
import com.example.Lipa.exceptions.SubscriptionStateException;

import lombok.Setter;

/**
 * Core aggregate root for the billing domain.
 * Encapsulates all lifecycle state transitions with invariant enforcement.
 */

@Setter
public class Subscription {

    private final UUID subscriptionId;
    private final UUID customerId;
    private final UUID planId;
    private SubscriptionStatus status;
    private String stripeSubscriptionId;
    private Instant currentPeriodStart;
    private Instant currentPeriodEnd;
    private Instant trialEnd;
    private Instant canceledAt;
    private Instant cancelAtPeriodEnd;
    private Integer seats;
    private String couponCode;
    private final Instant createdAt;
    private Instant updatedAt;

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    public static Subscription create(UUID customerId, UUID planId, Integer seats, Integer trialDays) {
        Instant now = Instant.now();
        Instant trialEnd = trialDays != null && trialDays > 0
                ? now.plus(trialDays, ChronoUnit.DAYS)
                : null;
        SubscriptionStatus status = trialEnd != null
                ? SubscriptionStatus.TRIALING
                : SubscriptionStatus.ACTIVE;

        return new Subscription(
                UUID.randomUUID(), customerId, planId, status,
                null, now, now.plus(30, ChronoUnit.DAYS),
                trialEnd, null, null, seats != null ? seats : 1,
                null, now, now
        );
    }

    // -------------------------------------------------------------------------
    // Business rules / state transitions
    // -------------------------------------------------------------------------

    public void activate() {
        if (status == SubscriptionStatus.CANCELED) {
            throw new SubscriptionStateException("Cannot activate a canceled subscription");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void markPastDue() {
        if (!status.isActive()) {
            throw new SubscriptionStateException("Only active subscriptions can be marked past due");
        }
        this.status = SubscriptionStatus.PAST_DUE;
        this.updatedAt = Instant.now();
    }

    /**
     * Schedules cancellation at period end (graceful cancel).
     */
    public void cancelAtPeriodEnd() {
        if (status == SubscriptionStatus.CANCELED) {
            throw new SubscriptionStateException("Subscription is already canceled");
        }
        this.cancelAtPeriodEnd = this.currentPeriodEnd;
        this.updatedAt = Instant.now();
    }

    /**
     * Immediate cancellation — no refund implied (handle separately).
     */
    public void cancelImmediately() {
        if (status == SubscriptionStatus.CANCELED) {
            throw new SubscriptionStateException("Subscription is already canceled");
        }
        this.status = SubscriptionStatus.CANCELED;
        this.canceledAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void renewPeriod(Instant newPeriodStart, Instant newPeriodEnd) {
        this.currentPeriodStart = newPeriodStart;
        this.currentPeriodEnd = newPeriodEnd;
        this.cancelAtPeriodEnd = null; // Clear scheduled cancellation on renew
        this.updatedAt = Instant.now();
    }

    public void pause() {
        if (status != SubscriptionStatus.ACTIVE) {
            throw new SubscriptionStateException("Only active subscriptions can be paused");
        }
        this.status = SubscriptionStatus.PAUSED;
        this.updatedAt = Instant.now();
    }

    public void resume() {
        if (status != SubscriptionStatus.PAUSED) {
            throw new SubscriptionStateException("Only paused subscriptions can be resumed");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void attachStripeSubscription(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
        this.updatedAt = Instant.now();
    }

    public boolean isScheduledForCancellation() {
        return cancelAtPeriodEnd != null;
    }

    public boolean isInTrial() {
        return status == SubscriptionStatus.TRIALING
                && trialEnd != null
                && Instant.now().isBefore(trialEnd);
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public Subscription(UUID subscriptionId, UUID customerId, UUID planId, SubscriptionStatus status,
        String stripeSubscriptionId, Instant currentPeriodStart,
        Instant currentPeriodEnd, Instant trialEnd, Instant canceledAt,
        Instant cancelAtPeriodEnd, Integer seats, String couponCode,
        Instant createdAt, Instant updatedAt) {
            this.subscriptionId = subscriptionId;
            this.customerId = customerId;
            this.planId = planId;
            this.status = status;
            this.stripeSubscriptionId = stripeSubscriptionId;
            this.currentPeriodStart = currentPeriodStart;
            this.currentPeriodEnd = currentPeriodEnd;
            this.trialEnd = trialEnd;
            this.canceledAt = canceledAt;
            this.cancelAtPeriodEnd = cancelAtPeriodEnd;
            this.seats = seats;
            this.couponCode = couponCode;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getSubscriptionId() { return subscriptionId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getPlanId() { return planId; }
    public SubscriptionStatus getStatus() { return status; }
    public String getStripeSubscriptionId() { return stripeSubscriptionId; }
    public Instant getCurrentPeriodStart() { return currentPeriodStart; }
    public Instant getCurrentPeriodEnd() { return currentPeriodEnd; }
    public Instant getTrialEnd() { return trialEnd; }
    public Instant getCanceledAt() { return canceledAt; }
    public Instant getCancelAtPeriodEnd() { return cancelAtPeriodEnd; }
    public Integer getSeats() { return seats; }
    public String getCouponCode() { return couponCode; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}