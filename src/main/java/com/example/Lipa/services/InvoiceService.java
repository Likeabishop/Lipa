package com.example.Lipa.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Lipa.entity.Invoice;
import com.example.Lipa.entity.Plan;
import com.example.Lipa.entity.Subscription;
import com.example.Lipa.event.BillingEvent;
import com.example.Lipa.exceptions.BillingException;
import com.example.Lipa.port.out.BillingEventPublisher;
import com.example.Lipa.repositories.InvoiceRepository;
import com.example.Lipa.repositories.PlanRepository;
import com.example.Lipa.repositories.SubscriptionRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final BillingEventPublisher eventPublisher;

    /**
     * Generates a new invoice for a subscription's current billing period.
     * Called by the renewal scheduler or by Stripe webhook on invoice.created.
     */
    public Invoice generateInvoice(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BillingException("Subscription not found: " + subscriptionId));

        Plan plan = planRepository.findById(subscription.getPlanId())
                .orElseThrow(() -> new BillingException("Plan not found: " + subscription.getPlanId()));

        Invoice invoice = Invoice.create(
                subscription.getCustomerId(),
                subscription.getSubscriptionId(),
                plan.getCurrency()
        );

        // Add the subscription line item
        String lineItemDescription = String.format(
                "%s × %d seat(s) — %s to %s",
                plan.getName(),
                subscription.getSeats(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd()
        );
        invoice.addLineItem(lineItemDescription,
                plan.getAmount().multiply(java.math.BigDecimal.valueOf(subscription.getSeats())), 1);

        // Generate sequential human-readable number
        long count = invoiceRepository.countInvoices() + 1;
        String invoiceNumber = String.format("INV-%d-%05d",
                java.time.Year.now().getValue(), count);

        invoice.finalize(invoiceNumber, Instant.now().plus(7, ChronoUnit.DAYS));
        invoice = invoiceRepository.save(invoice);

        log.info("Invoice generated id={} number={} total={}",
                invoice.getId(), invoice.getInvoiceNumber(), invoice.getTotal());

        eventPublisher.publish(new BillingEvent.InvoiceFinalized(
                invoice.getId(), invoice.getCustomerId(),
                invoice.getInvoiceNumber(), invoice.getTotal(),
                invoice.getCurrency(), invoice.getHostedInvoiceUrl()
        ));

        return invoice;
    }

    @Transactional(readOnly = true)
    public Invoice getInvoice(UUID invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BillingException("Invoice not found: " + invoiceId));
    }

    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesForCustomer(UUID customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

    public Invoice voidInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new BillingException("Invoice not found: " + invoiceId));
        invoice.voidInvoice();
        return invoiceRepository.save(invoice);
    }
}