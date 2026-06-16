package com.example.Lipa.exceptions;

public class SubscriptionNotFoundException extends BillingException {
    public SubscriptionNotFoundException(String id) {
        super("Subscription not found: " + id);
    }
}