package com.example.paymentsystem.merchant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MerchantTest {
    @Test
    @DisplayName("가맹점 이름이 null이면 생성할 수 없다")
    void shouldRejectNullName() {
        assertThrows(IllegalArgumentException.class, () -> new Merchant(null));
    }

    @Test
    @DisplayName("가맹점 이름이 공백이면 생성할 수 없다.")
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () -> new Merchant(" "));
    }

    @Test
    @DisplayName("가맹점 이름이 100자를 넘으면 생성할 수 없다.")
    void shouldRejectExceeding100Name() {
        String name = "A".repeat(101);
        assertThrows(IllegalArgumentException.class, () -> new Merchant(name));
    }

    @Test
    @DisplayName("가맹점 이름이 100자 이하면 정상적으로 생성된다.")
    void shouldAcceptBelow100Name() {
        String name = "A".repeat(100);
        assertDoesNotThrow(() -> new Merchant(name));
    }

    @Test
    @DisplayName("가맹점 생성 시 필수 정보가 올바르게 초기화된다.")
    void shouldInitializeMerchant() {
        Merchant merchant = new Merchant("테스트 상점");

        assertNotNull(merchant.getMerchantId());
        assertEquals("테스트 상점", merchant.getName());
        assertEquals(MerchantStatus.ACTIVE, merchant.getStatus());
        assertNotNull(merchant.getCreatedAt());
        assertEquals(merchant.getCreatedAt(), merchant.getUpdatedAt());
    }
}
