package com.example.Lipa.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Lipa.entity.Customer;
import com.example.Lipa.entity.Plan;
import com.example.Lipa.entity.Subscription;
import com.example.Lipa.event.BillingEvent;
import com.example.Lipa.exceptions.BillingException;
import com.example.Lipa.exceptions.SubscriptionNotFoundException;
import com.example.Lipa.port.in.SubscriptionUseCase;
import com.example.Lipa.port.out.BillingEventPublisher;
import com.example.Lipa.port.out.PaymentGateway;
import com.example.Lipa.repositories.CustomerRepository;
import com.example.Lipa.repositories.PlanRepository;
import com.example.Lipa.repositories.SubscriptionRepository;

import java.util.UUID;

/**
 * Orchestrates subscription lifecycle.
 * Delegates persistence, gateway, and event publishing to outbound adapters.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionService implements SubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final CustomerRepository customerRepository;
    private final PlanRepository planRepository;
    private final PaymentGateway paymentGateway;
    private final BillingEventPublisher eventPublisher;

    @Override
    public Subscription createSubscription(CreateSubscriptionCommand command) {
        log.info("Creating subscription for customer={} plan={}", command.customerId(), command.planId());

        Customer customer = customerRepository.findById(command.customerId())
                .orElseThrow(() -> new BillingException("Customer not found: " + command.customerId()));

        Plan plan = planRepository.findById(command.planId())
                .orElseThrow(() -> new BillingException("Plan not found: " + command.planId()));

        if (!plan.isActive()) {
            throw new BillingException("Plan is no longer available: " + plan.getName());
        }

        // Ensure customer exists in Stripe
        if (!customer.hasStripeAccount()) {
            String stripeCustomerId = paymentGateway.createCustomer(customer);
            customer.attachStripeCustomer(stripeCustomerId);
            customerRepository.save(customer);
        }

        // Create subscription domain object
        Subscription subscription = Subscription.create(
                command.customerId(),
                command.planId(),
                command.seats(),
                plan.getTrialDays()
        );

        // Persist first to get a stable ID before calling Stripe
        subscription = subscriptionRepository.save(subscription);

        log.info("Subscription created id={} status={}", subscription.getSubscriptionId(), subscription.getStatus());

        // Publish domain event
        eventPublisher.publish(new BillingEvent.SubscriptionCreated(
                subscription.getSubscriptionId(),
                customer.getCustomerId(),
                plan.getPlanId(),
                plan.getName(),
                subscription.isInTrial()
        ));

        return subscription;
    }

    @Override
    @Transactional(readOnly = true)
    public Subscription getSubscription(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId.toString()));
    }

    @Override
    public Subscription cancelSubscription(CancelSubscriptionCommand command) {
        log.info("Canceling subscription id={} immediate={}", command.subscriptionId(), command.immediate());

        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException(command.subscriptionId().toString()));

        if (command.immediate()) {
            subscription.cancelImmediately();
        } else {
            subscription.cancelAtPeriodEnd();
        }

        subscription = subscriptionRepository.save(subscription);

        eventPublisher.publish(new BillingEvent.SubscriptionCanceled(
                subscription.getSubscriptionId(),
                subscription.getCustomerId(),
                command.immediate()
        ));

        return subscription;
    }

    @Override
    public Subscription upgradeSubscription(UpgradeSubscriptionCommand command) {
        log.info("Upgrading subscription id={} to plan={}", command.subscriptionId(), command.newPlanId());

        Subscription subscription = subscriptionRepository.findById(command.subscriptionId())
                .orElseThrow(() -> new SubscriptionNotFoundException(command.subscriptionId().toString()));

        Plan newPlan = planRepository.findById(command.newPlanId())
                .orElseThrow(() -> new BillingException("Target plan not found: " + command.newPlanId()));

        if (!newPlan.isActive()) {
            throw new BillingException("Target plan is not active");
        }

        // Domain object doesn't know about plan details — create new subscription in the upgraded plan
        // In a real system you'd mutate the existing Stripe subscription and handle proration
        subscription.activate();
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription pauseSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId.toString()));
        subscription.pause();
        return subscriptionRepository.save(subscription);
    }

    @Override
    public Subscription resumeSubscription(UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new SubscriptionNotFoundException(subscriptionId.toString()));
        subscription.resume();
        return subscriptionRepository.save(subscription);
    }
}