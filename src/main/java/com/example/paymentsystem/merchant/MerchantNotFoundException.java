package com.example.paymentsystem.merchant;

import java.util.UUID;

public class MerchantNotFoundException extends RuntimeException {
    public MerchantNotFoundException(UUID merchantId) {
        super("가맹점을 찾을 수 없습니다: " + merchantId);
    }
}
