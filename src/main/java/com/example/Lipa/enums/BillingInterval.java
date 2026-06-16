package com.example.Lipa.enums;

public enum BillingInterval {
    MONTHLY,
    QUARTERLY,
    YEARLY;
 
    public int toMonths() {
        return switch (this) {
            case MONTHLY -> 1;
            case QUARTERLY -> 3;
            case YEARLY -> 12;
        };
    }
}