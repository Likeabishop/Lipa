package com.example.Lipa.infrastructure.adapter.out.stripe;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.Lipa.entity.Customer;
import com.example.Lipa.entity.Payment;
import com.example.Lipa.entity.Plan;
import com.example.Lipa.enums.BillingInterval;
import com.example.Lipa.exceptions.BillingException;
import com.example.Lipa.port.out.PaymentGateway;
import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Refund;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.RefundCreateParams;

import com.stripe.net.RequestOptions;

/**
 * Stripe adapter — implements the PaymentGateway port.
 *
 * <p>Stripe amounts are in the smallest currency unit (cents for ZAR/USD).
 * This adapter handles that conversion transparently.
 */
@Component
@Slf4j
public class StripePaymentGatewayAdapter implements PaymentGateway {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @PostConstruct
    void init() {
        Stripe.apiKey = stripeSecretKey;
        log.info("Stripe SDK initialized");
    }

    @Override
    public String createCustomer(Customer customer) {
        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(customer.getEmail())
                    .setName(customer.getName())
                    .putMetadata("internalCustomerId", customer.getCustomerId().toString())
                    .putMetadata("externalUserId", customer.getExternalUserId())
                    .build();

                    com.stripe.model.Customer stripeCustomer = com.stripe.model.Customer.create(params);
            log.info("Stripe customer created id={}", stripeCustomer.getId());
            return stripeCustomer.getId();
        } catch (StripeException e) {
            log.error("Failed to create Stripe customer", e);
            throw new BillingException(
                    "Failed to create customer in payment gateway", e);
        }
    }

    @Override
    public String createProduct(Plan plan) {
        try {
            ProductCreateParams params = ProductCreateParams.builder()
                    .setName(plan.getName())
                    .setDescription(plan.getDescription())
                    .putMetadata("internalPlanId", plan.getPlanId().toString())
                    .build();

            Product product = Product.create(params);
            return product.getId();
        } catch (StripeException e) {
            throw new BillingException(
                    "Failed to create product in payment gateway", e);
        }
    }

    @Override
    public String createPrice(Plan plan, String stripeProductId) {
        try {
            PriceCreateParams.Recurring.Interval interval = switch (plan.getBillingInterval()) {
                case MONTHLY -> PriceCreateParams.Recurring.Interval.MONTH;
                case QUARTERLY -> PriceCreateParams.Recurring.Interval.MONTH;  // interval_count=3
                case YEARLY -> PriceCreateParams.Recurring.Interval.YEAR;
            };

            PriceCreateParams.Builder builder = PriceCreateParams.builder()
                    .setProduct(stripeProductId)
                    .setUnitAmount(toStripeAmount(plan.getAmount()))
                    .setCurrency(plan.getCurrency().toLowerCase())
                    .setRecurring(PriceCreateParams.Recurring.builder()
                            .setInterval(interval)
                            .setIntervalCount(plan.getBillingInterval() == BillingInterval.QUARTERLY ? 3L : 1L)
                            .build());

            Price price = Price.create(builder.build());
            return price.getId();
        } catch (StripeException e) {
            throw new BillingException(
                    "Failed to create price in payment gateway", e);
        }
    }

    @Override
    public ChargeResult charge(Payment payment, String stripeCustomerId) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(toStripeAmount(payment.getAmount()))
                    .setCurrency(payment.getCurrency().toLowerCase())
                    .setCustomer(stripeCustomerId)
                    .setPaymentMethod(payment.getPaymentMethodId())
                    .setConfirm(true)
                    .setOffSession(true)   // SaaS recurring — no 3DS redirect
                    .putMetadata("paymentId", payment.getPaymentId().toString())
                    .putMetadata("invoiceId", payment.getInvoiceId().toString())
                    .build();
            
            RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(payment.getIdempotencyKey()) // ← moved here
                .build();

            PaymentIntent intent = PaymentIntent.create(params, requestOptions);

            boolean succeeded = "succeeded".equals(intent.getStatus());
            String chargeId = (intent.getLatestChargeObject() != null)
                    ? intent.getLatestChargeObject().getId()
                    : null;

            return new ChargeResult(intent.getId(), chargeId, succeeded, null, null);

        } catch (CardException e) {
            // Card-level declines — user-actionable
            log.warn("Card declined for payment={} code={} message={}",
                    payment.getPaymentId(), e.getCode(), e.getMessage());
            return new ChargeResult(null, null, false, e.getCode(), e.getMessage());

        } catch (StripeException e) {
            log.error("Stripe error during charge for payment={}", payment.getPaymentId(), e);
            return new ChargeResult(null, null, false, "STRIPE_ERROR", e.getMessage());
        }
    }

    @Override
    public RefundResult refund(String stripeChargeId, BigDecimal amount, String reason) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setCharge(stripeChargeId)
                    .setAmount(toStripeAmount(amount))
                    .setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER)
                    .build();

            Refund refund = Refund.create(params);
            return new RefundResult(refund.getId(), "succeeded".equals(refund.getStatus()));
        } catch (StripeException e) {
            log.error("Refund failed for charge={}", stripeChargeId, e);
            return new RefundResult(null, false);
        }
    }

    /**
     * Stripe expects amounts in smallest currency unit (cents/cents).
     * R 99.99 ZAR → 9999
     */
    private long toStripeAmount(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.HALF_UP)
                .longValueExact();
    }
}