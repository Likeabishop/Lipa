package com.example.Lipa.enums;

public enum SubscriptionStatus {
    TRIALING,
    ACTIVE,
    PAST_DUE,
    CANCELED,
    UNPAID,
    PAUSED;
 
    public boolean isActive() {
        return this == ACTIVE || this == TRIALING;
    }
}
