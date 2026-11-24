package com.example.paymentsystem.apikey;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "merchant_api_keys")
public class MerchantApiKey {
    public enum Status { ACTIVE, REVOKED }
    public enum Environment { TEST }

    @Id
    @Column(name = "key_id", nullable = false)
    private UUID keyId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "secret_hash", nullable = false)
    private byte[] secretHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "environment", nullable = false)
    private Environment environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "merchant_api_key_scopes", joinColumns = @JoinColumn(name = "key_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 30)
    private Set<ApiKeyScope> scopes = new HashSet<>();

    protected MerchantApiKey() { }

    public MerchantApiKey(UUID keyId, UUID merchantId, byte[] secretHash,
                          Set<ApiKeyScope> scopes, Instant expiresAt) {
        if (keyId == null || merchantId == null || secretHash == null || secretHash.length != 32
                || scopes == null || scopes.isEmpty() || scopes.contains(null)) {
            throw new IllegalArgumentException("Invalid API key metadata");
        }
        this.keyId = keyId;
        this.merchantId = merchantId;
        this.secretHash = secretHash.clone();
        this.scopes = new HashSet<>(scopes);
        this.environment = Environment.TEST;
        this.status = Status.ACTIVE;
        this.createdAt = Instant.now();
        this.expiresAt = expiresAt;
    }

    public boolean isUsable(Instant now) {
        return status == Status.ACTIVE && (expiresAt == null || now.isBefore(expiresAt));
    }

    public void revoke() {
        if (status == Status.ACTIVE) {
            status = Status.REVOKED;
            revokedAt = Instant.now();
        }
    }

    public UUID getKeyId() { return keyId; }
    public UUID getMerchantId() { return merchantId; }
    public byte[] getSecretHash() { return secretHash.clone(); }
    public Set<ApiKeyScope> getScopes() { return Set.copyOf(scopes); }
    public Status getStatus() { return status; }
    public Environment getEnvironment() { return environment; }
    public Instant getExpiresAt() { return expiresAt; }
}
