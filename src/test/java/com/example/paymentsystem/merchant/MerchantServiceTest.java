package com.example.paymentsystem.merchant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantServiceTest {
    @Mock
    private MerchantRepository merchantRepository;

    private MerchantService merchantService;

    @BeforeEach
    void setUp() {
        merchantService = new MerchantService(merchantRepository);
    }

    @Test
    @DisplayName("가맹점을 저장하는 비즈니스 로직을 테스트한다")
    void shouldRegisterMerchant() {
        // Given
        Merchant savedMerchant = new Merchant("테스트 가맹점");

        // 여기에 when(...).thenReturn(...) 작성
        when(merchantRepository.save(any(Merchant.class))).thenReturn(savedMerchant);

        // When
        Merchant result = merchantService.registerMerchant("테스트 가맹점");

        // Then
        assertSame(savedMerchant, result);
        verify(merchantRepository).save(argThat(m -> "테스트 가맹점".equals(m.getName())));
    }

    @Test
    @DisplayName("존재하는 가맹점을 ID로 조회한다")
    void shouldGetMerchant() {
        // Given
        Merchant merchant = new Merchant("테스트 가맹점");
        UUID merchantId = merchant.getMerchantId();

        when(merchantRepository.findById(merchantId))
                .thenReturn(Optional.of(merchant));

        // When
        Merchant result = merchantService.getMerchant(merchantId);

        // Then
        assertSame(merchant, result);
        verify(merchantRepository).findById(merchantId);
    }

    @Test
    @DisplayName("존재하지 않는 가맹점 ID를 조회하면 예외가 발생한다")
    void shouldThrowWhenMerchantNotFound() {
        // Given
        UUID unknownId = UUID.randomUUID();

        when(merchantRepository.findById(unknownId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> merchantService.getMerchant(unknownId));

        verify(merchantRepository).findById(unknownId);
    }
}
