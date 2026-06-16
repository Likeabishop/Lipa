package com.example.Lipa.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.Lipa.entity.Invoice;
import com.example.Lipa.enums.InvoiceStatus;
import com.example.Lipa.infrastructure.adapter.out.mapper.InvoiceMapper;
import com.example.Lipa.infrastructure.adapter.out.persistence.entity.InvoiceEntity;
import com.example.Lipa.repositories.InvoiceJpaRepository;
import com.example.Lipa.repositories.InvoiceRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final InvoiceJpaRepository jpaRepository;
    private final InvoiceMapper mapper;

    @Override
    public Invoice save(Invoice invoice) {
        InvoiceEntity entity = mapper.toEntity(invoice);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Invoice> findById(UUID invoiceId) {
        return jpaRepository.findById(invoiceId).map(mapper::toDomain);
    }

    @Override
    public List<Invoice> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerCustomerId(customerId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<Invoice> findByStatus(InvoiceStatus status) {
        return jpaRepository.findByStatus(status)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Invoice> findByStripeInvoiceId(String stripeInvoiceId) {
        return jpaRepository.findByStripeInvoiceId(stripeInvoiceId)
                .map(mapper::toDomain);
    }

    @Override
    public long countInvoices() {
        return jpaRepository.count();
    }
}