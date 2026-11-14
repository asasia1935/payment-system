package com.example.paymentsystem.merchant;

import java.util.UUID;

public record MerchantResponse(UUID merchantId, String name, MerchantStatus status) {
}
