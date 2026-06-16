package com.example.Lipa.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.Lipa.entity.Subscription;
import com.example.Lipa.enums.SubscriptionStatus;
import com.example.Lipa.infrastructure.adapter.out.mapper.SubscriptionPersistenceMapper;
import com.example.Lipa.repositories.SubscriptionJpaRepository;
import com.example.Lipa.repositories.SubscriptionRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter — bridges the domain port to JPA.
 * Domain never references Spring Data or JPA directly.
 */
@Component
@RequiredArgsConstructor
public class SubscriptionPersistenceAdapter implements SubscriptionRepository {

    private final SubscriptionJpaRepository jpaRepository;
    private final SubscriptionPersistenceMapper mapper;

    @Override
    public Subscription save(Subscription subscription) {
        var entity = mapper.toEntity(subscription);
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Subscription> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Subscription> findByCustomerId(UUID customerId) {
        return jpaRepository.findByCustomerCustomerId(customerId).map(mapper::toDomain);
    }

    @Override
    public List<Subscription> findByStatus(SubscriptionStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Subscription> findSubscriptionsWithTrialEndingBefore(Instant cutoff) {
        return jpaRepository.findTrialsEndingBefore(cutoff).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}