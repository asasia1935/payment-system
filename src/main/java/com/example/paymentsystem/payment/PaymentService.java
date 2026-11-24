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
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new MerchantNotActiveException(merchantId);
        }
        if (paymentRepository.existsByMerchantIdAndOrderId(merchantId, orderId)) {
            throw new DuplicatePaymentOrderException(merchantId, orderId);
        }
        Payment payment = new Payment(merchantId, orderId, amount);
        return paymentRepository.save(payment);
    }

    /**
     * Legacy internal lookup retained for existing tests/internal processing only.
     * Never expose this method to untrusted clients without merchant scoping.
     */
    @Transactional(readOnly = true)
    public Payment getPayment(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    /** Authenticated merchant lookup: scoped in SQL to prevent cross-merchant access. */
    @Transactional(readOnly = true)
    public Payment getPayment(UUID merchantId, UUID paymentId) {
        return paymentRepository.findByPaymentIdAndMerchantId(paymentId, merchantId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }
}
