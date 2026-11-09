package com.example.paymentsystem.merchant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MerchantService {
    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public Merchant registerMerchant(String name) {
        Merchant merchant = new Merchant(name);

        return merchantRepository.save(merchant);
    }

    @Transactional(readOnly = true)
    public Merchant getMerchant(UUID merchantId) {
        return merchantRepository.findById(merchantId).orElseThrow();
    }
}
