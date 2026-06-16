package com.example.Lipa.infrastructure.adapter.out.mapper;

import org.mapstruct.Mapper;
import com.example.Lipa.entity.Subscription;
import com.example.Lipa.infrastructure.adapter.out.persistence.entity.SubscriptionEntity;

@Mapper(componentModel = "spring")
public abstract class SubscriptionPersistenceMapper {

    public SubscriptionEntity toEntity(Subscription domain) {
        SubscriptionEntity entity = new SubscriptionEntity();
        entity.setSubscriptionId(domain.getSubscriptionId());
        entity.getCustomer().setCustomerId(domain.getCustomerId());
        entity.getPlan().setPlanId(domain.getPlanId());
        entity.setStatus(domain.getStatus());
        entity.setStripeSubscriptionId(domain.getStripeSubscriptionId());
        entity.setCurrentPeriodStart(domain.getCurrentPeriodStart());
        entity.setCurrentPeriodEnd(domain.getCurrentPeriodEnd());
        entity.setTrialEnd(domain.getTrialEnd());
        entity.setCanceledAt(domain.getCanceledAt());
        entity.setCancelAtPeriodEnd(domain.getCancelAtPeriodEnd());
        entity.setSeats(domain.getSeats());
        entity.setCouponCode(domain.getCouponCode());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public Subscription toDomain(SubscriptionEntity entity) {
        return new Subscription(
            entity.getSubscriptionId(),
            entity.getCustomer().getCustomerId(),
            entity.getPlan().getPlanId(),
            entity.getStatus(),
            entity.getStripeSubscriptionId(),
            entity.getCurrentPeriodStart(),
            entity.getCurrentPeriodEnd(),
            entity.getTrialEnd(),
            entity.getCanceledAt(),
            entity.getCancelAtPeriodEnd(),
            entity.getSeats(),
            entity.getCouponCode(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}