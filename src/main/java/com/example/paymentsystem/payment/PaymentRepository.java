package com.example.paymentsystem.payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // 가맹점 ID, 주문 ID로 존재하는지 확인하는 메서드
    boolean existsByMerchantIdAndOrderId(UUID merchantId, String orderId);
}
