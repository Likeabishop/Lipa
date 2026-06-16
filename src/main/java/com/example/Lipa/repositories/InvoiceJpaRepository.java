package com.example.Lipa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Lipa.enums.InvoiceStatus;
import com.example.Lipa.infrastructure.adapter.out.persistence.entity.InvoiceEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceJpaRepository extends JpaRepository<InvoiceEntity, UUID> {
    List<InvoiceEntity> findByCustomerCustomerId(UUID customerId);
    List<InvoiceEntity> findByStatus(InvoiceStatus status);
    Optional<InvoiceEntity> findByStripeInvoiceId(String stripeInvoiceId);
}