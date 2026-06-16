package com.example.Lipa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.Lipa.enums.SubscriptionStatus;
import com.example.Lipa.infrastructure.adapter.out.persistence.entity.SubscriptionEntity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionJpaRepository extends JpaRepository<SubscriptionEntity, UUID> {
    Optional<SubscriptionEntity> findByCustomerCustomerId(UUID customerId);
    List<SubscriptionEntity> findByStatus(SubscriptionStatus status);

    @Query("SELECT s FROM SubscriptionEntity s WHERE s.status = 'TRIALING' AND s.trialEnd < :cutoff")
    List<SubscriptionEntity> findTrialsEndingBefore(Instant cutoff);
}