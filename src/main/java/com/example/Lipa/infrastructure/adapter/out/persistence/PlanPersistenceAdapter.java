package com.example.Lipa.infrastructure.adapter.out.persistence;

import com.example.Lipa.entity.Plan;
import com.example.Lipa.repositories.PlanRepository;
import com.example.Lipa.infrastructure.adapter.out.persistence.entity.PlanEntity;
import com.example.Lipa.repositories.PlanJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class PlanPersistenceAdapter implements PlanRepository {

    private final PlanJpaRepository jpaRepository;

    public PlanPersistenceAdapter(PlanJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Plan save(Plan plan) {
        PlanEntity entity = toEntity(plan);
        PlanEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Plan> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Plan> findAllActive() {
        return jpaRepository.findAll()
                .stream()
                .filter(PlanEntity::isActive)
                .map(this::toDomain)
                .toList();
    }

    // --- lightweight internal mapping (no separate class needed yet)

    private PlanEntity toEntity(Plan plan) {
        PlanEntity entity = new PlanEntity();
        entity.setPlanId(plan.getPlanId());
        entity.setName(plan.getName());
        entity.setAmount(plan.getAmount());
        entity.setCurrency(plan.getCurrency());
        entity.setBillingInterval(plan.getBillingInterval());
        entity.setTrialDays(plan.getTrialDays());
        entity.setMaxSeats(plan.getMaxSeats());
        entity.setStripeProductId(plan.getStripeProductId());
        entity.setStripePriceId(plan.getStripePriceId());
        entity.setActive(plan.isActive());
        return entity;
    }

    private Plan toDomain(PlanEntity entity) {
        Plan plan = new Plan();
        plan.setPlanId(entity.getPlanId());
        plan.setName(entity.getName());
        plan.setDescription(entity.getDescription());
        plan.setAmount(entity.getAmount());
        plan.setCurrency(entity.getCurrency());
        plan.setBillingInterval(entity.getBillingInterval());
        plan.setTrialDays(entity.getTrialDays());
        plan.setActive(entity.isActive());
        plan.setMaxSeats(entity.getMaxSeats());
        plan.setStripeProductId(entity.getStripeProductId());
        plan.setStripePriceId(entity.getStripePriceId());
        return plan;
    }
}