package com.example.paymentsystem.payment;

import com.example.paymentsystem.apikey.*;
import com.example.paymentsystem.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import({SecurityConfig.class, ApiKeyAuthenticationProvider.class})
class PaymentSecurityMvcTest {
    @Autowired MockMvc mockMvc;

    @MockitoBean MerchantApiKeyService keyService;
    @MockitoBean PaymentService paymentService;

    private final UUID merchantA = UUID.randomUUID();
    private final UUID merchantB = UUID.randomUUID();
    private static final String KEY = "sk_test_opaque_test_value";

    private void allow(Set<ApiKeyScope> scopes) {
        when(keyService.verify(KEY)).thenReturn(Optional.of(
                new MerchantApiKeyService.VerifiedKey(UUID.randomUUID(), merchantA, scopes)));
    }

    @Test
    void noKeyReturns401() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"O1\",\"amount\":1000}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(paymentService);
    }

    @Test
    void invalidKeyReturns401() throws Exception {
        when(keyService.verify(KEY)).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/payments")
                        .header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"O1\",\"amount\":1000}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(paymentService);
    }

    @Test
    void readOnlyKeyCannotCreatePayment() throws Exception {
        allow(Set.of(ApiKeyScope.PAYMENT_READ));
        mockMvc.perform(post("/api/payments")
                        .header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\"O1\",\"amount\":1000}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(paymentService);
    }

    @Test
    void createUsesAuthenticatedMerchantNotUserInput() throws Exception {
        allow(Set.of(ApiKeyScope.PAYMENT_CREATE));
        Payment payment = new Payment(merchantA, "O1", 1000);
        when(paymentService.createPayment(merchantA, "O1", 1000)).thenReturn(payment);

        mockMvc.perform(post("/api/payments")
                        .header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"merchantId\":\"" + merchantB + "\",\"orderId\":\"O1\",\"amount\":1000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.merchantId").value(merchantA.toString()));
        verify(paymentService).createPayment(merchantA, "O1", 1000);
        verifyNoMoreInteractions(paymentService);
    }

    @Test
    void invalidBodyReturns400AndDoesNotCreatePayment() throws Exception {
        allow(Set.of(ApiKeyScope.PAYMENT_CREATE));
        mockMvc.perform(post("/api/payments")
                        .header("X-API-Key", KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderId\":\" \",\"amount\":0}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(paymentService);
    }

    @Test
    void merchantScopedLookupDoesNotAcceptClientSelectedMerchant() throws Exception {
        allow(Set.of(ApiKeyScope.PAYMENT_READ));
        Payment payment = new Payment(merchantA, "O2", 3000);
        when(paymentService.getPayment(merchantA, payment.getPaymentId())).thenReturn(payment);

        mockMvc.perform(get("/api/payments/{id}", payment.getPaymentId())
                        .header("X-API-Key", KEY)
                        .param("merchantId", merchantB.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.merchantId").value(merchantA.toString()));
        verify(paymentService).getPayment(merchantA, payment.getPaymentId());
    }
}
