package com.example.paymentsystem.merchant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {
    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping
    public ResponseEntity<MerchantResponse> registerMerchant(@Valid @RequestBody RegisterMerchantRequest request) {
        String name = request.name();

        Merchant merchant = merchantService.registerMerchant(name);

        MerchantResponse response = new MerchantResponse(
                merchant.getMerchantId(), merchant.getName(), merchant.getStatus());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{merchantId}")
    public ResponseEntity<MerchantResponse> getMerchant(@PathVariable UUID merchantId) {
        Merchant merchant = merchantService.getMerchant(merchantId);

        MerchantResponse response = new MerchantResponse(
                merchant.getMerchantId(), merchant.getName(), merchant.getStatus());

        return ResponseEntity.ok(response);
    }
}
