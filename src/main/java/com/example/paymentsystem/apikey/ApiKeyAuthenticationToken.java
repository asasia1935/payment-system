package com.example.paymentsystem.apikey;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public final class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    private final UUID merchantId;
    private String rawKey;

    private ApiKeyAuthenticationToken(String rawKey) {
        super(null);
        this.rawKey = rawKey;
        this.merchantId = null;
        super.setAuthenticated(false);
    }

    private ApiKeyAuthenticationToken(UUID merchantId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.merchantId = merchantId;
        this.rawKey = null;
        super.setAuthenticated(true);
    }

    public static ApiKeyAuthenticationToken unauthenticated(String rawKey) {
        return new ApiKeyAuthenticationToken(rawKey);
    }

    public static ApiKeyAuthenticationToken authenticated(UUID merchantId, Set<ApiKeyScope> scopes) {
        return new ApiKeyAuthenticationToken(merchantId, scopes.stream()
                .map(s -> new SimpleGrantedAuthority(s.name()))
                .toList());
    }

    @Override
    public Object getCredentials() { return rawKey; }

    @Override
    public Object getPrincipal() { return merchantId; }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        rawKey = null;
    }
}
