package com.example.paymentsystem.payment;

import java.util.UUID;

public class DuplicatePaymentOrderException extends RuntimeException {
    public DuplicatePaymentOrderException(UUID merchantId, String orderId) {
        super("중복된 결제 주문입니다: " + merchantId + ", " + orderId);
    }
}
