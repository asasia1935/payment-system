package com.example.paymentsystem.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentTest {
    @Test
    @DisplayName("결제를 생성하면 CREATED 상태로 초기화된다")
    void shouldCreatePaymentWithCreatedStatus() {
        // Given
        UUID merchantId = UUID.randomUUID();

        // When
        Payment payment = new Payment(merchantId, "ORDER-001", 10000L);

        // Then
        assertEquals(PaymentStatus.CREATED, payment.getStatus());
    }


    @Test
    @DisplayName("결제를 생성시 금액이 0원이면 생성할 수 없다")
    void shouldRejectAmountZero() {
        // Given
        UUID merchantId = UUID.randomUUID();

        // When
        // Then
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(merchantId, "ORDER-001", 0));
    }

    @Test
    @DisplayName("결제를 생성시 금액이 음수면 생성할 수 없다")
    void shouldRejectAmountZeroBelow() {
        // Given
        UUID merchantId = UUID.randomUUID();

        // When
        // Then
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(merchantId, "ORDER-001", -1));
    }

    @Test
    @DisplayName("가맹점 ID가 null이면 생성할 수 없다")
    void shouldRejectNullMerchantID() {
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(null, "ORDER-001", 10000L));
    }

    @Test
    @DisplayName("주문 ID가 null이면 생성할 수 없다.")
    void shouldRejectNullOrderID() {
        UUID merchantId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(merchantId, null, 10000L));
    }

    @Test
    @DisplayName("주문 ID가 공백이면 생성할 수 없다.")
    void shouldRejectBlankOrderId() {
        UUID merchantId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(merchantId, "  ", 10000L));
    }

    @Test
    @DisplayName("주문 ID가 100자를 넘으면 생성할 수 없다.")
    void shouldRejectExceeding100OrderId() {
        UUID merchantId = UUID.randomUUID();
        String orderId = "A".repeat(101);
        assertThrows(IllegalArgumentException.class,
                () -> new Payment(merchantId, orderId, 10000L));
    }

    //[ ] createdAt과 updatedAt이 동일한지 확인
}
