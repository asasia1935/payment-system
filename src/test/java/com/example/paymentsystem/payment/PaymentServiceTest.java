package com.example.paymentsystem.payment;


import com.example.paymentsystem.merchant.Merchant;
import com.example.paymentsystem.merchant.MerchantNotActiveException;
import com.example.paymentsystem.merchant.MerchantNotFoundException;
import com.example.paymentsystem.merchant.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private MerchantRepository merchantRepository;
    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(merchantRepository, paymentRepository);
    }

    @Test
    @DisplayName("결제를 저장하는 비즈니스 로직을 테스트한다")
    void shouldCreatePayment() {
        // Given
        // 가맹점 부터 생성
        Merchant savedMerchant = new Merchant("테스트 가맹점");
        UUID merchantId = savedMerchant.getMerchantId();
        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(savedMerchant));
        // 결제 생성
        Payment savedPayment = new Payment(merchantId, "ORDER-001", 10000L);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        // When
        Payment result = paymentService.createPayment(merchantId, "ORDER-001", 10000L);

        // Then
        assertSame(savedPayment, result);
        verify(paymentRepository).save(argThat(m ->
                merchantId.equals(m.getMerchantId())
                        && "ORDER-001".equals(m.getOrderId())
                        && m.getAmount() == 10000L
                        && m.getStatus() == PaymentStatus.CREATED));
    }

    @Test
    @DisplayName("존재하지 않는 가맹점은 결제를 생성할 수 없다")
    void shouldRejectPaymentForUnknownMerchant() {
        // Given
        UUID merchantId = UUID.randomUUID();

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(MerchantNotFoundException.class,
                () -> paymentService.createPayment(merchantId, "ORDER-001", 10000L));

        verify(paymentRepository, never())
                .save(any(Payment.class));
    }

    @Test
    @DisplayName("SUSPENDED 가맹점은 결제를 생성할 수 없다")
    void shouldRejectPaymentForSuspendedMerchant() {
        // Given
        Merchant merchant = new Merchant("테스트 가맹점");
        merchant.suspend();

        UUID merchantId = merchant.getMerchantId();

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));

        // When & Then
        assertThrows(MerchantNotActiveException.class,
                () -> paymentService.createPayment(merchantId, "ORDER-001", 10000L));

        verifyNoInteractions(paymentRepository);
    }

    @Test
    @DisplayName("동일 가맹점의 중복 주문번호로 결제를 생성할 수 없다")
    void shouldRejectDuplicatePaymentOrder() {
        // Given
        Merchant merchant = new Merchant("테스트 가맹점");
        UUID merchantId = merchant.getMerchantId();
        String orderId = "ORDER-001";

        when(merchantRepository.findById(merchantId)).thenReturn(Optional.of(merchant));

        // 동일 가맹점, 동일 주문번호가 이미 존재하는 상황
        when(paymentRepository.existsByMerchantIdAndOrderId(merchantId, orderId)).thenReturn(true);

        // When & Then
        assertThrows(DuplicatePaymentOrderException.class,
                () -> paymentService.createPayment(merchantId, orderId, 10000L));

        // 중복 확인은 실행됐는지 검증
        verify(paymentRepository).existsByMerchantIdAndOrderId(merchantId, orderId);

        // 실제 저장은 시도하지 않았는지 검증
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("존재하는 결제 ID로 결제 정보를 조회할 수 있다")
    void shouldGetPaymentById() {
        // Given
        UUID merchantId = UUID.randomUUID();
        Payment payment = new Payment(merchantId, "ORDER-001", 10000L);

        UUID paymentId = payment.getPaymentId();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        // When
        Payment result = paymentService.getPayment(paymentId);

        // Then
        assertSame(payment, result);

        verify(paymentRepository).findById(paymentId);
    }

    @Test
    @DisplayName("존재하지 않는 결제 ID를 조회하면 예외가 발생한다")
    void shouldRejectUnknownPaymentId() {
        // Given
        UUID paymentId = UUID.randomUUID();

        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPayment(paymentId));

        verify(paymentRepository).findById(paymentId);
    }
}
