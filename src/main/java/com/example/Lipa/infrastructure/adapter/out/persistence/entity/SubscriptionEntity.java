package com.example.Lipa.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import com.example.Lipa.enums.SubscriptionStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscriptions_customer_id", columnList = "customer_id"),
        @Index(name = "idx_subscriptions_status", columnList = "status"),
        @Index(name = "idx_subscriptions_trial_end", columnList = "trial_end")
})
@Getter
@Setter
public class SubscriptionEntity {

    @Id
    @UuidGenerator
    @Column(name = "subscription_id", updatable = false, nullable = false)
    private UUID subscriptionId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private PlanEntity plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "stripe_subscription_id", length = 100)
    private String stripeSubscriptionId;

    @Column(name = "current_period_start")
    private Instant currentPeriodStart;

    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    @Column(name = "trial_end")
    private Instant trialEnd;

    @Column(name = "canceled_at")
    private Instant canceledAt;

    @Column(name = "cancel_at_period_end")
    private Instant cancelAtPeriodEnd;

    @Column(nullable = false)
    private Integer seats = 1;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}