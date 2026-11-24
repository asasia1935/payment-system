package com.example.paymentsystem.apikey;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MerchantApiKeyRepository extends JpaRepository<MerchantApiKey, UUID> {
}
