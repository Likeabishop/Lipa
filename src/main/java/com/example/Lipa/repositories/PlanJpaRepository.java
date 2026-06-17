package com.example.Lipa.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Lipa.infrastructure.adapter.out.persistence.entity.PlanEntity;

public interface PlanJpaRepository extends JpaRepository<PlanEntity, UUID> {
    
}
