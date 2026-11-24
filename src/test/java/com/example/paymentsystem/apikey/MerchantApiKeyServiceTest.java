package com.example.paymentsystem.apikey;

import com.example.paymentsystem.merchant.Merchant;
import com.example.paymentsystem.merchant.MerchantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MerchantApiKeyServiceTest {
    @Mock MerchantApiKeyRepository keyRepository;
    @Mock MerchantRepository merchantRepository;
    MerchantApiKeyService service;

    @BeforeEach
    void setUp() {
        service = new MerchantApiKeyService(keyRepository, merchantRepository);
    }

    @Test
    void issuedKeyIsNotStoredInPlainTextAndCanBeVerified() {
        Merchant merchant = new Merchant("Sample shop");
        when(merchantRepository.findById(merchant.getMerchantId())).thenReturn(Optional.of(merchant));

        final MerchantApiKey[] saved = new MerchantApiKey[1];
        when(keyRepository.save(any(MerchantApiKey.class))).thenAnswer(invocation -> {
            saved[0] = invocation.getArgument(0);
            return saved[0];
        });

        MerchantApiKeyService.IssuedKey issued = service.issueTestKey(
                merchant.getMerchantId(), Set.of(ApiKeyScope.PAYMENT_CREATE), null);

        assertTrue(issued.rawKey().startsWith("sk_test_" + issued.keyId() + "."));
        assertNotNull(saved[0]);
        assertEquals(32, saved[0].getSecretHash().length);
        assertFalse(issued.toString().contains(issued.rawKey()), "Never log raw API keys");

        when(keyRepository.findById(issued.keyId())).thenReturn(Optional.of(saved[0]));
        var verified = service.verify(issued.rawKey()).orElseThrow();
        assertEquals(merchant.getMerchantId(), verified.merchantId());
        assertEquals(Set.of(ApiKeyScope.PAYMENT_CREATE), verified.scopes());

        assertTrue(service.verify(issued.rawKey() + "bad").isEmpty());
        assertTrue(service.verify("sk_test_invalid").isEmpty());

        saved[0].revoke();
        assertTrue(service.verify(issued.rawKey()).isEmpty());
    }

    @Test
    void suspendedMerchantCannotIssueKey() {
        Merchant merchant = new Merchant("Suspended");
        merchant.suspend();
        when(merchantRepository.findById(merchant.getMerchantId())).thenReturn(Optional.of(merchant));

        assertThrows(IllegalStateException.class, () -> service.issueTestKey(
                merchant.getMerchantId(), Set.of(ApiKeyScope.PAYMENT_READ), null));
        verifyNoInteractions(keyRepository);
    }

    @Test
    void expiryMustBeFuture() {
        assertThrows(IllegalArgumentException.class, () -> service.issueTestKey(
                java.util.UUID.randomUUID(), Set.of(ApiKeyScope.PAYMENT_READ), Instant.now().minusSeconds(1)));
    }
}
