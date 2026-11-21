package com.example.paymentsystem.payment;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(UUID paymentId) {
        super("존재하지 않는 결제 ID입니다: " + paymentId);
    }
}
