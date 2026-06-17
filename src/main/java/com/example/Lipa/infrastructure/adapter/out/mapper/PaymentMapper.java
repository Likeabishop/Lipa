package com.example.Lipa.infrastructure.adapter.out.mapper;

import com.example.Lipa.entity.Payment;
import com.example.Lipa.infrastructure.adapter.out.persistence.entity.PaymentEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentEntity toEntity(Payment payment) {
        PaymentEntity entity = new PaymentEntity();

        entity.setPaymentId(payment.getPaymentId());
        entity.getCustomer().setCustomerId(payment.getCustomerId());
        entity.setAmount(payment.getAmount());
        entity.setCurrency(payment.getCurrency());
        entity.setStatus(payment.getStatus());
        entity.setIdempotencyKey(payment.getIdempotencyKey());
        entity.setStripePaymentIntentId(payment.getStripePaymentIntentId());

        return entity;
    }

    public Payment toDomain(PaymentEntity entity) {
        return Payment.rehydrate(
            entity.getPaymentId(),
            entity.getCustomer().getCustomerId(),
            entity.getInvoice().getInvoiceId(),
            entity.getStatus(),
            entity.getAmount(),
            entity.getRefundedAmount(),
            entity.getCurrency(),
            entity.getStripePaymentIntentId(),
            entity.getStripeChargeId(),
            entity.getPaymentMethodId(),
            entity.getFailureCode(),
            entity.getFailureMessage(),
            entity.getIdempotencyKey(),
            entity.getProcessedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}