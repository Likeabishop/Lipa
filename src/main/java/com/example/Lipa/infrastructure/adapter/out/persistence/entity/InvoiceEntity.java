package com.example.Lipa.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import com.example.Lipa.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices", indexes = {
        @Index(name = "idx_invoices_customer_id", columnList = "customer_id"),
        @Index(name = "idx_invoices_subscription_id", columnList = "subscription_id"),
        @Index(name = "idx_invoices_status", columnList = "status")
})
@Getter
@Setter
public class InvoiceEntity {

    @Id
    @UuidGenerator
    @Column(name = "invoice_id", updatable = false, nullable = false)
    private UUID invoiceId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private SubscriptionEntity subscription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvoiceStatus status;

    @Column(name = "stripe_invoice_id", length = 100)
    private String stripeInvoiceId;

    @Column(name = "invoice_number", unique = true, length = 30)
    private String invoiceNumber;

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "hosted_invoice_url", length = 500)
    private String hostedInvoiceUrl;

    /**
     * Line items stored as JSONB — avoids unnecessary JOIN for read-heavy invoice queries.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "line_items", columnDefinition = "jsonb")
    private String lineItemsJson;

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