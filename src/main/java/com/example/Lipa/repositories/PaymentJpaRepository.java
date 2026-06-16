package com.example.Lipa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Lipa.infrastructure.adapter.out.persistence.entity.PaymentEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);
    List<PaymentEntity> findByCustomerId(UUID customerId);
    Optional<PaymentEntity> findByStripePaymentIntentId(String paymentIntentId);
}