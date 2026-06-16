package com.example.Lipa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Lipa.infrastructure.adapter.out.persistence.entity.CustomerEntity;

import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
    Optional<CustomerEntity> findByExternalUserId(String externalUserId);
    Optional<CustomerEntity> findByStripeCustomerId(String stripeCustomerId);
}