package com.example.paymentsystem.payment;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(nullable = false)
    private long amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Payment() {

    }

    public Payment(UUID merchantId, String orderId, long amount) {
        // 1. 입력값 검증
        if (merchantId == null) {
            throw new IllegalArgumentException("가맹점 ID가 올바르지 않습니다.");
        }

        if (orderId == null) {
            throw new IllegalArgumentException("주문 ID가 올바르지 않습니다.");
        }

        if (orderId.isBlank()) {
            throw new IllegalArgumentException("주문 ID는 공백일 수 없습니다.");
        }

        if (orderId.length() > 100) {
            throw new IllegalArgumentException("주문 ID는 100자를 초과할 수 없습니다.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("결제 가격은 0원 이하일 수 없습니다.");
        }

        // 2. paymentId 생성
        // 3. 필드 초기화
        this.paymentId = UUID.randomUUID();
        this.merchantId = merchantId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = "KRW";
        this.status = PaymentStatus.CREATED;

        // 4. 생성 시각 초기화
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Getter
    public UUID getMerchantId() { return merchantId; }
    public UUID getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public long getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public PaymentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
