package com.example.Lipa.port.out;

import java.math.BigDecimal;

import com.example.Lipa.entity.Customer;
import com.example.Lipa.entity.Payment;
import com.example.Lipa.entity.Plan;

/**
 * Outbound port for the Stripe payment gateway.
 * Any PSP (PayFast, PayStack, Ozow) could replace this adapter.
 */
public interface PaymentGateway {

    record ChargeResult(String paymentIntentId, String chargeId, boolean succeeded,
                        String failureCode, String failureMessage) {}

    record RefundResult(String refundId, boolean succeeded) {}

    String createCustomer(Customer customer);

    String createProduct(Plan plan);

    String createPrice(Plan plan, String stripeProductId);

    ChargeResult charge(Payment payment, String stripeCustomerId);

    RefundResult refund(String stripeChargeId, BigDecimal amount, String reason);
}