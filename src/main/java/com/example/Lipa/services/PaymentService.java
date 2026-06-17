package com.example.Lipa.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Lipa.entity.Customer;
import com.example.Lipa.entity.Invoice;
import com.example.Lipa.entity.Payment;
import com.example.Lipa.entity.PaymentUseCase;
import com.example.Lipa.event.BillingEvent;
import com.example.Lipa.exceptions.BillingException;
import com.example.Lipa.exceptions.PaymentFailedException;
import com.example.Lipa.port.out.BillingEventPublisher;
import com.example.Lipa.port.out.PaymentGateway;
import com.example.Lipa.repositories.CustomerRepository;
import com.example.Lipa.repositories.InvoiceRepository;
import com.example.Lipa.repositories.PaymentRepository;

import java.util.UUID;

/**
 * Payment orchestration with idempotency guarantee.
 *
 * <p>An idempotency key is checked before initiating any charge — if the same
 * key is replayed (network retry, duplicate webhook) the original Payment
 * record is returned without double-charging the customer.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService implements PaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentGateway paymentGateway;
    private final BillingEventPublisher eventPublisher;

    @Override
    public Payment processPayment(ProcessPaymentCommand command) {
        log.info("Processing payment for invoice={} amount={} {}",
                command.invoiceId(), command.amount(), command.currency());

        // --- Idempotency check ---
        String idempotencyKey = command.idempotencyKey() != null
                ? command.idempotencyKey()
                : UUID.randomUUID().toString();

        return paymentRepository.findByIdempotencyKey(idempotencyKey)
                .map(existing -> {
                    log.warn("Duplicate payment request detected — returning existing payment id={}", existing.getPaymentId());
                    return existing;
                })
                .orElseGet(() -> executePayment(command, idempotencyKey));
    }

    private Payment executePayment(ProcessPaymentCommand command, String idempotencyKey) {
        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new BillingException("Customer not found: " + command.customerId()));

        Invoice invoice = invoiceRepository.findById(command.invoiceId())
                .orElseThrow(() -> new BillingException("Invoice not found: " + command.invoiceId()));

        // Create payment record (PENDING)
        Payment payment = Payment.create(
                command.customerId(),
                command.invoiceId(),
                command.amount(),
                command.currency(),
                command.paymentMethodId()
        );
        payment = paymentRepository.save(payment);

        try {
            // Mark PROCESSING before gateway call
            payment.markProcessing(null);
            paymentRepository.save(payment);

            // Delegate to Stripe adapter
            PaymentGateway.ChargeResult result = paymentGateway.charge(payment, customer.getStripeCustomerId());

            if (result.succeeded()) {
                payment.succeed(result.chargeId());
                payment = paymentRepository.save(payment);

                // Mark invoice paid
                invoice.markPaid(payment.getProcessedAt());
                invoiceRepository.save(invoice);

                eventPublisher.publish(new BillingEvent.PaymentSucceeded(
                        payment.getPaymentId(), customer.getCustomerId(), invoice.getId(),
                        payment.getAmount(), payment.getCurrency()
                ));

                log.info("Payment succeeded id={} chargeId={}", payment.getPaymentId(), result.chargeId());
            } else {
                payment.fail(result.failureCode(), result.failureMessage());
                payment = paymentRepository.save(payment);

                eventPublisher.publish(new BillingEvent.PaymentFailed(
                        payment.getPaymentId(), customer.getCustomerId(), invoice.getId(),
                        result.failureCode(), result.failureMessage()
                ));

                throw new PaymentFailedException(result.failureMessage(), result.failureCode());
            }
        } catch (PaymentFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during payment processing", e);
            payment.fail("INTERNAL_ERROR", e.getMessage());
            paymentRepository.save(payment);
            throw new BillingException("Payment processing failed", e);
        }

        return payment;
    }

    @Override
    public Payment refundPayment(RefundPaymentCommand command) {
        log.info("Refunding payment id={} amount={}", command.paymentId(), command.refundAmount());

        Payment payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new BillingException("Payment not found: " + command.paymentId()));

        PaymentGateway.RefundResult result = paymentGateway.refund(
                payment.getStripeChargeId(), command.refundAmount(), command.reason()
        );

        if (!result.succeeded()) {
            throw new BillingException("Refund failed for payment: " + payment.getPaymentId());
        }

        payment.refund(command.refundAmount());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BillingException("Payment not found: " + paymentId));
    }
}