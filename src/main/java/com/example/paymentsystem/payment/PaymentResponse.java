package com.example.paymentsystem.payment;

import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(UUID paymentId, UUID merchantId, String orderId, long amount,
                              String currency, String status, Instant createdAt, Instant updatedAt) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.getPaymentId(), payment.getMerchantId(),
                payment.getOrderId(), payment.getAmount(), payment.getCurrency(),
                payment.getStatus().name(), payment.getCreatedAt(), payment.getUpdatedAt());
    }
}
