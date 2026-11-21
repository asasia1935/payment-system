package com.example.paymentsystem.merchant;

import java.util.UUID;

public class MerchantNotActiveException extends RuntimeException {
    public MerchantNotActiveException(UUID merchantId) {
        super("해당 가맹점은 Active 상태가 아닙니다: " + merchantId);
    }


}
