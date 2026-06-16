package com.example.Lipa.port.in;

import java.util.UUID;

import com.example.Lipa.entity.Subscription;

/**
 * Inbound port — subscription use cases exposed to REST adapters.
 * Keeps the application core decoupled from HTTP concerns.
 */
public interface SubscriptionUseCase {

    record CreateSubscriptionCommand(
            UUID customerId,
            UUID planId,
            String paymentMethodId,
            Integer seats,
            String couponCode
    ) {}

    record CancelSubscriptionCommand(
            UUID subscriptionId,
            boolean immediate   // false = cancel at period end
    ) {}

    record UpgradeSubscriptionCommand(
            UUID subscriptionId,
            UUID newPlanId,
            boolean prorated
    ) {}

    Subscription createSubscription(CreateSubscriptionCommand command);

    Subscription getSubscription(UUID subscriptionId);

    Subscription cancelSubscription(CancelSubscriptionCommand command);

    Subscription upgradeSubscription(UpgradeSubscriptionCommand command);

    Subscription pauseSubscription(UUID subscriptionId);

    Subscription resumeSubscription(UUID subscriptionId);
}