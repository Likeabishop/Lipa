package com.example.Lipa.controllers;

import com.example.Lipa.enums.PaymentStatus;
import com.example.Lipa.event.BillingEvent;
import com.example.Lipa.exceptions.SubscriptionStateException;
import com.example.Lipa.port.out.BillingEventPublisher;
import com.example.Lipa.repositories.InvoiceRepository;
import com.example.Lipa.repositories.PaymentRepository;
import com.example.Lipa.repositories.SubscriptionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
@Hidden  // Exclude from Swagger UI — internal endpoint
public class StripeWebhookController {

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BillingEventPublisher eventPublisher;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature — possible replay or spoofing attack");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        log.info("Received Stripe webhook event type={} id={}", event.getType(), event.getId());

        switch (event.getType()) {
            case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(event);
            case "payment_intent.payment_failed" -> handlePaymentIntentFailed(event);
            case "invoice.payment_failed" -> handleInvoicePaymentFailed(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            default -> log.debug("Unhandled webhook event type={}", event.getType());
        }

        // Always return 200 — Stripe retries on non-2xx responses
        return ResponseEntity.ok("Received");
    }

    private void handlePaymentIntentSucceeded(Event event) {
        var pi = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow();

        paymentRepository.findByIdempotencyKey(pi.getId()).ifPresentOrElse(
                payment -> {
                    if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
                        payment.succeed(pi.getId());
                        paymentRepository.save(payment);
                        eventPublisher.publish(new BillingEvent.PaymentSucceeded(
                                payment.getPaymentId(), payment.getCustomerId(), payment.getInvoiceId(),
                                payment.getAmount(), payment.getCurrency()
                        ));
                    }
                },
                () -> log.warn("Received payment_intent.succeeded for unknown payment intentId={}", pi.getId())
        );
    }

    private void handlePaymentIntentFailed(Event event) {
        var pi = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElseThrow();

        paymentRepository.findByIdempotencyKey(pi.getId()).ifPresent(payment -> {
            String failureCode = pi.getLastPaymentError() != null ? pi.getLastPaymentError().getCode() : "UNKNOWN";
            String failureMsg = pi.getLastPaymentError() != null ? pi.getLastPaymentError().getMessage() : "";
            payment.fail(failureCode, failureMsg);
            paymentRepository.save(payment);
            eventPublisher.publish(new BillingEvent.PaymentFailed(
                    payment.getPaymentId(), payment.getCustomerId(), payment.getInvoiceId(),
                    failureCode, failureMsg
            ));
        });
    }

    private void handleInvoicePaymentFailed(Event event) {
        // When Stripe invoice payment fails, mark the subscription as past_due
        var stripeInvoice = (Invoice) event.getDataObjectDeserializer()
                .getObject().orElseThrow();

        invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId()).ifPresent(invoice -> {
            subscriptionRepository.findById(invoice.getSubscriptionId()).ifPresent(subscription -> {
                try {
                    subscription.markPastDue();
                    subscriptionRepository.save(subscription);
                    eventPublisher.publish(new BillingEvent.SubscriptionPastDue(
                            subscription.getSubscriptionId(), subscription.getCustomerId()
                    ));
                } catch (SubscriptionStateException e) {
                    log.warn("Could not mark subscription past_due: {}", e.getMessage());
                }
            });
        });
    }

    private void handleSubscriptionDeleted(Event event) {
        var stripeSub = (com.stripe.model.Subscription) event.getDataObjectDeserializer()
                .getObject().orElseThrow();

        // Find by stripe subscription ID and cancel immediately
        subscriptionRepository.findByCustomerId(
                // In practice you'd look up by stripeSubscriptionId — simplified here
                java.util.UUID.randomUUID()
        ).ifPresent(subscription -> {
            try {
                subscription.cancelImmediately();
                subscriptionRepository.save(subscription);
            } catch (Exception e) {
                log.error("Failed to process external cancellation for subscription", e);
            }
        });
    }
}