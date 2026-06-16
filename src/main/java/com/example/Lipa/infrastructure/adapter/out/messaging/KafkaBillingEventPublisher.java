package com.example.Lipa.infrastructure.adapter.out.messaging;

import com.example.Lipa.event.BillingEvent;
import com.example.Lipa.port.out.BillingEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter for publishing domain events.
 *
 * <p>Topic naming convention: billing.{event-type-kebab-case}
 * e.g. billing.subscription-created, billing.payment-succeeded
 *
 * <p>The key is always the relevant entity UUID for consumer partition affinity.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaBillingEventPublisher implements BillingEventPublisher {

    private static final String TOPIC_PREFIX = "billing.";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(BillingEvent event) {
        try {
            String topic = TOPIC_PREFIX + toKebabCase(event.type());
            String payload = objectMapper.writeValueAsString(event);
            String key = extractKey(event);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, key, payload);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event type={} topic={}", event.type(), topic, ex);
                } else {
                    log.debug("Published event type={} topic={} partition={} offset={}",
                            event.type(), topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("Error serializing billing event type={}", event.type(), e);
            // Don't throw — event publishing failure should not roll back the business transaction.
            // In production, use an Outbox pattern for guaranteed delivery.
        }
    }

    private String extractKey(BillingEvent event) {
        return switch (event) {
            case BillingEvent.SubscriptionCreated e -> e.subscriptionId().toString();
            case BillingEvent.SubscriptionCanceled e -> e.subscriptionId().toString();
            case BillingEvent.SubscriptionPastDue e -> e.subscriptionId().toString();
            case BillingEvent.PaymentSucceeded e -> e.paymentId().toString();
            case BillingEvent.PaymentFailed e -> e.paymentId().toString();
            case BillingEvent.InvoiceFinalized e -> e.invoiceId().toString();
            case BillingEvent.TrialWillEnd e -> e.subscriptionId().toString();
        };
    }

    private String toKebabCase(String input) {
        return input.toLowerCase().replace("_", "-");
    }
}