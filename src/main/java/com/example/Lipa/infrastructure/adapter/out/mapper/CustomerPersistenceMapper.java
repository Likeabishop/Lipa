package com.example.Lipa.infrastructure.adapter.out.mapper;

import com.example.Lipa.entity.Customer;
import com.example.Lipa.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerPersistenceMapper {

    public CustomerEntity toEntity(Customer customer) {
        if (customer == null) return null;

        CustomerEntity entity = new CustomerEntity();
        entity.setCustomerId(customer.getCustomerId());
        entity.setExternalUserId(customer.getExternalUserId());
        entity.setStripeCustomerId(customer.getStripeCustomerId());

        return entity;
    }

    public Customer toDomain(CustomerEntity entity) {
        if (entity == null) return null;

        return Customer.reconstruct(
            entity.getCustomerId(),
            entity.getExternalUserId(),
            entity.getEmail(),
            entity.getName(),
            entity.getStripeCustomerId(),
            entity.getDefaultPaymentMethodId(),
            entity.getCurrency(),
            entity.isTaxExempt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
    );
    }
}