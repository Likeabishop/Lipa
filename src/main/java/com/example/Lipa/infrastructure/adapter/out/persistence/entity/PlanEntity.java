package com.example.Lipa.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import com.example.Lipa.enums.BillingInterval;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plans")
@Getter
@Setter
public class PlanEntity {

    @Id
    @UuidGenerator
    @Column(name = "plan_id", updatable = false, nullable = false)
    private UUID planId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_interval", nullable = false, length = 20)
    private BillingInterval billingInterval;

    @Column(name = "trial_days")
    private Integer trialDays;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "max_seats")
    private Integer maxSeats;

    @Column(name = "stripe_product_id", length = 100)
    private String stripeProductId;

    @Column(name = "stripe_price_id", length = 100, unique = true)
    private String stripePriceId;

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
    void preUpdate() { updatedAt = Instant.now(); }
}