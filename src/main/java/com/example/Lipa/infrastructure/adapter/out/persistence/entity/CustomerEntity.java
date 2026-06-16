package com.example.Lipa.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_stripe_customer_id", columnList = "stripe_customer_id", unique = true)
})
@Getter
@Setter
public class CustomerEntity {

    @Id
    @UuidGenerator
    @Column(name = "customer_id", updatable = false, nullable = false)
    private UUID customerId;

    @Column(name = "external_user_id", nullable = false, unique = true, length = 200)
    private String externalUserId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 255)
    private String name;

    @Column(name = "stripe_customer_id", length = 100)
    private String stripeCustomerId;

    @Column(name = "default_payment_method_id", length = 100)
    private String defaultPaymentMethodId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "tax_exempt", nullable = false)
    private boolean taxExempt = false;

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