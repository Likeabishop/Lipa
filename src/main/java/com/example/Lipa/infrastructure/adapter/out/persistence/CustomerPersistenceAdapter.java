package com.example.Lipa.infrastructure.adapter.out.persistence;

import com.example.Lipa.entity.Customer;
import com.example.Lipa.repositories.CustomerRepository;
import com.example.Lipa.repositories.CustomerJpaRepository;
import com.example.Lipa.infrastructure.adapter.out.mapper.CustomerPersistenceMapper;
import com.example.Lipa.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CustomerPersistenceAdapter implements CustomerRepository {

    private final CustomerJpaRepository jpaRepository;
    private final CustomerPersistenceMapper mapper;

    public CustomerPersistenceAdapter(
            CustomerJpaRepository jpaRepository,
            CustomerPersistenceMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity entity = mapper.toEntity(customer);
        CustomerEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(UUID customerId) {
        return jpaRepository.findById(customerId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByExternalUserId(String externalUserId) {
        return jpaRepository.findByExternalUserId(externalUserId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByStripeCustomerId(String stripeCustomerId) {
        return jpaRepository.findByStripeCustomerId(stripeCustomerId)
                .map(mapper::toDomain);
    }
}