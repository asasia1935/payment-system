package com.example.paymentsystem.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterMerchantRequest(@NotBlank @Size(max = 100) String name) {
}
