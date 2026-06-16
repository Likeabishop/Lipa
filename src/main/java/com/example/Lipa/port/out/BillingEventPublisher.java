package com.example.Lipa.port.out;

import com.example.Lipa.event.BillingEvent;

/**
 * Outbound port for publishing domain events to Kafka.
 * Decouples application layer from Kafka infrastructure.
 */
public interface BillingEventPublisher {
    void publish(BillingEvent event);
}