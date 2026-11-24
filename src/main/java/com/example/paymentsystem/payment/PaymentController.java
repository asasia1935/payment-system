package com.example.paymentsystem.payment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@Valid @RequestBody CreatePaymentRequest request,
                                                  Authentication authentication) {
        UUID merchantId = (UUID) authentication.getPrincipal();
        Payment payment = paymentService.createPayment(merchantId, request.orderId(), request.amount());
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentResponse.from(payment));
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse findById(@PathVariable UUID paymentId, Authentication authentication) {
        UUID merchantId = (UUID) authentication.getPrincipal();
        return PaymentResponse.from(paymentService.getPayment(merchantId, paymentId));
    }
}
