package com.example.Lipa.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.Lipa.DTOs.BillingDtos;
import com.example.Lipa.port.in.SubscriptionUseCase;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription lifecycle management")
public class SubscriptionController {

    private final SubscriptionUseCase subscriptionUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('BILLING_ADMIN') or hasRole('SERVICE_ACCOUNT')")
    @Operation(summary = "Create a new subscription for a customer")
    public ResponseEntity<BillingDtos.SubscriptionResponse> create(
            @Valid @RequestBody BillingDtos.CreateSubscriptionRequest request) {

        var subscription = subscriptionUseCase.createSubscription(
                new SubscriptionUseCase.CreateSubscriptionCommand(
                        request.customerId(),
                        request.planId(),
                        request.paymentMethodId(),
                        request.seats(),
                        request.couponCode()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BillingDtos.SubscriptionResponse.from(subscription));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a subscription by ID")
    public ResponseEntity<BillingDtos.SubscriptionResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                BillingDtos.SubscriptionResponse.from(subscriptionUseCase.getSubscription(id))
        );
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel a subscription (immediately or at period end)")
    public ResponseEntity<BillingDtos.SubscriptionResponse> cancel(
            @PathVariable UUID id,
            @RequestBody BillingDtos.CancelSubscriptionRequest request) {

        var subscription = subscriptionUseCase.cancelSubscription(
                new SubscriptionUseCase.CancelSubscriptionCommand(id, request.immediate())
        );
        return ResponseEntity.ok(BillingDtos.SubscriptionResponse.from(subscription));
    }

    @PostMapping("/{id}/upgrade")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upgrade or downgrade subscription plan")
    public ResponseEntity<BillingDtos.SubscriptionResponse> upgrade(
            @PathVariable UUID id,
            @Valid @RequestBody BillingDtos.UpgradeSubscriptionRequest request) {

        var subscription = subscriptionUseCase.upgradeSubscription(
                new SubscriptionUseCase.UpgradeSubscriptionCommand(id, request.newPlanId(), request.prorated())
        );
        return ResponseEntity.ok(BillingDtos.SubscriptionResponse.from(subscription));
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Pause an active subscription")
    public ResponseEntity<BillingDtos.SubscriptionResponse> pause(@PathVariable UUID id) {
        return ResponseEntity.ok(
                BillingDtos.SubscriptionResponse.from(subscriptionUseCase.pauseSubscription(id))
        );
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Resume a paused subscription")
    public ResponseEntity<BillingDtos.SubscriptionResponse> resume(@PathVariable UUID id) {
        return ResponseEntity.ok(
                BillingDtos.SubscriptionResponse.from(subscriptionUseCase.resumeSubscription(id))
        );
    }
}