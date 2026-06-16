package com.example.Lipa.exceptions;

public class PaymentFailedException extends BillingException {
    private final String stripeCode;
 
    public PaymentFailedException(String message, String stripeCode) {
        super(message);
        this.stripeCode = stripeCode;
    }
 
    public String getStripeCode() { return stripeCode; }
}