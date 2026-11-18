package com.example.paymentsystem.payment;

import com.example.paymentsystem.merchant.Merchant;
import com.example.paymentsystem.merchant.MerchantRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {
    private final PaymentRepository paymentRepository;
    private final MerchantRepository merchantRepository;

    @Autowired
    PaymentRepositoryTest(PaymentRepository paymentRepository, MerchantRepository merchantRepository) {
        this.paymentRepository = paymentRepository;
        this.merchantRepository = merchantRepository;
    }

    @Autowired
    private EntityManager entityManager;

    @DisplayName("결제를 저장하고 ID로 조회할 수 있다")
    @Test
    void shouldSaveAndFindPayment() {
        // Given: 가맹점 생성 후 저장
        Merchant merchant = new Merchant("테스트 가맹점");
        merchantRepository.save(merchant);
        entityManager.flush();
        entityManager.clear();

        // 결제 생성
        UUID merchantId = merchant.getMerchantId();
        Payment payment = new Payment(merchantId, "ORDER-001", 10000L);

        // When: Repository로 저장하고 ID 조회
        paymentRepository.save(payment);

        entityManager.flush();
        entityManager.clear();

        Payment found = paymentRepository.findById(payment.getPaymentId()).orElseThrow();

        // Then: 저장한 ID와 조회한 ID 및 금액 비교
        assertEquals("ORDER-001", found.getOrderId());
        assertEquals(10000L, found.getAmount());
    }

    @Test
    @DisplayName("존재하지 않는 가맹점 ID로 결제를 생성할 수 없다")
    void shouldRejectSavingPaymentWithUnknownMerchantId() {
        // Given: 존재하지 않는 ID로 결제 생성
        UUID merchantId = UUID.randomUUID();
        Payment payment = new Payment(merchantId, "ORDER-001", 10000L);

        // When, Then: Repository로 저장시 예외 발생
        // (DataIntegrityViolationException 예외 발생 -> DB 무결성 제약조건 위반 시)
        assertThrows(
                DataIntegrityViolationException.class,
                () -> paymentRepository.saveAndFlush(payment)
        );
    }

    @Test
    @DisplayName("(가맹점 ID, 주문 ID)는 중복될 수 없다")
    void shouldNotDuplicateMerchantIDOrderID() {
        // Given: 가맹점 생성 후 첫번째 결제 저장
        Merchant merchant = new Merchant("테스트 가맹점");
        merchantRepository.saveAndFlush(merchant);
        UUID merchantId = merchant.getMerchantId();
        Payment payment1 = new Payment(merchantId, "ORDER-001", 10000L);
        Payment payment2 = new Payment(merchantId, "ORDER-001", 10000L);

        paymentRepository.saveAndFlush(payment1);

        // When, Then: 두번째 결제로 같은 (가맹점 ID, 주문 ID)는 저장 시 예외 발생
        assertThrows(
                DataIntegrityViolationException.class,
                () -> paymentRepository.saveAndFlush(payment2)
        );
    }
}
