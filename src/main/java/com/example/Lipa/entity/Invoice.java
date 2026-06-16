package com.example.Lipa.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.example.Lipa.enums.InvoiceStatus;
import com.example.Lipa.exceptions.BillingException;

import lombok.Builder;
import lombok.Getter;

/**
 * Invoice aggregate — represents a billing document for one billing cycle.
 * Line items are value objects embedded within the invoice.
 */

@Getter
@Builder
public class Invoice {

    private final UUID invoiceId;
    private final UUID customerId;
    private final UUID subscriptionId;
    private InvoiceStatus status;
    private String stripeInvoiceId;
    private String invoiceNumber;           // Human-readable: INV-2024-00001
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private String currency;
    private Instant dueDate;
    private Instant paidAt;
    private String hostedInvoiceUrl;        // Stripe-hosted PDF link
    private final List<InvoiceLineItem> lineItems;
    private final Instant createdAt;
    private Instant updatedAt;

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    public static Invoice create(UUID customerId, UUID subscriptionId, String currency) {
        return new Invoice(
                UUID.randomUUID(), customerId, subscriptionId, InvoiceStatus.DRAFT,
                null, null, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, currency.toUpperCase(),
                null, null, null, new ArrayList<>(), Instant.now(), Instant.now()
        );
    }

    // -------------------------------------------------------------------------
    // Business rules
    // -------------------------------------------------------------------------

    public void addLineItem(String description, BigDecimal unitAmount, int quantity) {
        if (status != InvoiceStatus.DRAFT) {
            throw new BillingException("Cannot modify a finalized invoice");
        }
        lineItems.add(new InvoiceLineItem(UUID.randomUUID(), description, unitAmount, quantity));
        recalculate();
    }

    public void applyDiscount(BigDecimal discountAmount) {
        if (status != InvoiceStatus.DRAFT) {
            throw new BillingException("Cannot apply discount to a finalized invoice");
        }
        this.discountAmount = discountAmount;
        recalculate();
    }

    public void applyTax(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
        recalculate();
    }

    public void finalize(String invoiceNumber, Instant dueDate) {
        if (status != InvoiceStatus.DRAFT) {
            throw new BillingException("Invoice is already finalized");
        }
        this.invoiceNumber = invoiceNumber;
        this.dueDate = dueDate;
        this.status = InvoiceStatus.OPEN;
        this.updatedAt = Instant.now();
    }

    public void markPaid(Instant paidAt) {
        if (status != InvoiceStatus.OPEN) {
            throw new BillingException("Only open invoices can be marked as paid");
        }
        this.status = InvoiceStatus.PAID;
        this.paidAt = paidAt;
        this.updatedAt = Instant.now();
    }

    public void voidInvoice() {
        if (status == InvoiceStatus.PAID) {
            throw new BillingException("Paid invoices cannot be voided — issue a credit note instead");
        }
        this.status = InvoiceStatus.VOID;
        this.updatedAt = Instant.now();
    }

    public void attachStripeInvoice(String stripeInvoiceId, String hostedInvoiceUrl) {
        this.stripeInvoiceId = stripeInvoiceId;
        this.hostedInvoiceUrl = hostedInvoiceUrl;
        this.updatedAt = Instant.now();
    }

    private void recalculate() {
        this.subtotal = lineItems.stream()
                .map(InvoiceLineItem::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = subtotal.add(taxAmount).subtract(discountAmount);
        this.updatedAt = Instant.now();
    }

    // -------------------------------------------------------------------------
    // Value Object: Line Item
    // -------------------------------------------------------------------------

    public record InvoiceLineItem(
            UUID id,
            String description,
            BigDecimal unitAmount,
            int quantity
    ) {
        public BigDecimal lineTotal() {
            return unitAmount.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    private Invoice(UUID invoiceId, UUID customerId, UUID subscriptionId, InvoiceStatus status,
                    String stripeInvoiceId, String invoiceNumber, BigDecimal subtotal,
                    BigDecimal taxAmount, BigDecimal discountAmount, BigDecimal total,
                    String currency, Instant dueDate, Instant paidAt, String hostedInvoiceUrl,
                    List<InvoiceLineItem> lineItems, Instant createdAt, Instant updatedAt) {
        this.invoiceId = invoiceId;
        this.customerId = customerId;
        this.subscriptionId = subscriptionId;
        this.status = status;
        this.stripeInvoiceId = stripeInvoiceId;
        this.invoiceNumber = invoiceNumber;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
        this.total = total;
        this.currency = currency;
        this.dueDate = dueDate;
        this.paidAt = paidAt;
        this.hostedInvoiceUrl = hostedInvoiceUrl;
        this.lineItems = lineItems;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public UUID getId() { return invoiceId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getSubscriptionId() { return subscriptionId; }
    public InvoiceStatus getStatus() { return status; }
    public String getStripeInvoiceId() { return stripeInvoiceId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotal() { return total; }
    public String getCurrency() { return currency; }
    public Instant getDueDate() { return dueDate; }
    public Instant getPaidAt() { return paidAt; }
    public String getHostedInvoiceUrl() { return hostedInvoiceUrl; }
    public List<InvoiceLineItem> getLineItems() { return Collections.unmodifiableList(lineItems); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}