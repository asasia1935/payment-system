package com.example.paymentsystem.apikey;

/** The permission granted to one merchant API key, not a platform-wide user role. */
public enum ApiKeyScope {
    PAYMENT_CREATE,
    PAYMENT_READ,
    PAYMENT_CANCEL
}
