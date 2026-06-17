package com.example.Lipa.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.Lipa.entity.Payment;
import com.example.Lipa.infrastructure.adapter.out.mapper.PaymentMapper;
import com.example.Lipa.repositories.PaymentJpaRepository;
import com.example.Lipa.repositories.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public Payment save(Payment payment) {
        return paymentMapper.toDomain(
                paymentJpaRepository.save(
                        paymentMapper.toEntity(payment)
                )
        );
    }

    @Override
    public Optional<Payment> findById(UUID paymentId) {
        return paymentJpaRepository.findById(paymentId)
                .map(paymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return paymentJpaRepository.findByIdempotencyKey(idempotencyKey)
                .map(paymentMapper::toDomain);
    }

    @Override
    public List<Payment> findByCustomerId(UUID customerId) {
        return paymentJpaRepository.findByCustomerCustomerId(customerId)
                .stream()
                .map(paymentMapper::toDomain)
                .toList();
    }
}