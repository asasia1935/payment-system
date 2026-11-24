package com.example.paymentsystem.apikey;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {
    private final MerchantApiKeyService keyService;

    public ApiKeyAuthenticationProvider(MerchantApiKeyService keyService) {
        this.keyService = keyService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        String rawKey = (String) authentication.getCredentials();
        MerchantApiKeyService.VerifiedKey verified = keyService.verify(rawKey)
                .orElseThrow(() -> new BadCredentialsException("Invalid API key"));
        return ApiKeyAuthenticationToken.authenticated(verified.merchantId(), verified.scopes());
    }

    @Override
    public boolean supports(Class<?> authenticationType) {
        return ApiKeyAuthenticationToken.class.isAssignableFrom(authenticationType);
    }
}
