package com.example.Lipa.repositories;

import java.util.Optional;
import java.util.UUID;

import com.example.Lipa.entity.Payment;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(UUID paymentId);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    java.util.List<Payment> findByCustomerId(UUID customerId);
}