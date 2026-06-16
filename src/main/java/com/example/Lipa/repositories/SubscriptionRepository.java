package com.example.Lipa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.Lipa.entity.Subscription;
import com.example.Lipa.enums.SubscriptionStatus;

public interface SubscriptionRepository {
    Subscription save(Subscription subscription);
    Optional<Subscription> findById(UUID id);
    Optional<Subscription> findByCustomerId(UUID customerId);
    List<Subscription> findByStatus(SubscriptionStatus status);
    List<Subscription> findSubscriptionsWithTrialEndingBefore(java.time.Instant cutoff);
    void deleteById(UUID id);
}