package com.example.paymentsystem.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    boolean existsByMerchantIdAndOrderId(UUID merchantId, String orderId);
    Optional<Payment> findByPaymentIdAndMerchantId(UUID paymentId, UUID merchantId);
}
