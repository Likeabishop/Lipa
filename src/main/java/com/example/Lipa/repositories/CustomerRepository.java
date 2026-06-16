package com.example.Lipa.repositories;

import java.util.Optional;
import java.util.UUID;

import com.example.Lipa.entity.Customer;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID customerId);
    Optional<Customer> findByExternalUserId(String externalUserId);
    Optional<Customer> findByStripeCustomerId(String stripeCustomerId);
}