package com.example.Lipa.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.Lipa.entity.Plan;

public interface PlanRepository {
    Plan save(Plan plan);
    Optional<Plan> findById(UUID id);
    List<Plan> findAllActive();
}