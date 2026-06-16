package com.example.Lipa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.Lipa.entity.Invoice;
import com.example.Lipa.enums.InvoiceStatus;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(UUID invoiceId);
    List<Invoice> findByCustomerId(UUID customerId);
    List<Invoice> findByStatus(InvoiceStatus status);
    Optional<Invoice> findByStripeInvoiceId(String stripeInvoiceId);
    long countInvoices(); // For generating invoice numbers
}