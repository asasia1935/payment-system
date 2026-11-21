package com.example.paymentsystem.payment;

import com.example.paymentsystem.merchant.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {
    private final MerchantRepository merchantRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(MerchantRepository merchantRepository, PaymentRepository paymentRepository) {
        this.merchantRepository = merchantRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment createPayment(UUID merchantId, String orderId, long amount) {

        // 1. 가맹점 조회
        Merchant merchant = merchantRepository.findById(merchantId).
                orElseThrow(() -> new MerchantNotFoundException(merchantId));

        // 2. ACTIVE 상태 검증
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new MerchantNotActiveException(merchantId);
        }

        // 3. 중복 주문번호 검증
        if(paymentRepository.existsByMerchantIdAndOrderId(merchantId, orderId)) {
            throw new DuplicatePaymentOrderException(merchantId, orderId);
        }

        // 4. Payment 생성 및 저장
        Payment payment = new Payment(merchantId, orderId, amount);
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

}
