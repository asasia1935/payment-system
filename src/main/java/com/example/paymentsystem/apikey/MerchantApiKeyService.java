package com.example.paymentsystem.apikey;

import com.example.paymentsystem.merchant.Merchant;
import com.example.paymentsystem.merchant.MerchantRepository;
import com.example.paymentsystem.merchant.MerchantStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class MerchantApiKeyService {
    private static final String PREFIX = "sk_test_";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MerchantApiKeyRepository keyRepository;
    private final MerchantRepository merchantRepository;

    public MerchantApiKeyService(MerchantApiKeyRepository keyRepository, MerchantRepository merchantRepository) {
        this.keyRepository = keyRepository;
        this.merchantRepository = merchantRepository;
    }

    /**
     * Internal-only key provisioning. Never expose without authenticated merchant ownership checks.
     * The secret is returned exactly once and is NOT stored in the database.
     */
    @Transactional
    public IssuedKey issueTestKey(UUID merchantId, Set<ApiKeyScope> scopes, Instant expiresAt) {
        if (scopes == null || scopes.isEmpty() || scopes.contains(null)) {
            throw new IllegalArgumentException("At least one valid scope is required");
        }
        if (expiresAt != null && !expiresAt.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Expiry must be in the future");
        }
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Merchant does not exist"));
        if (merchant.getStatus() != MerchantStatus.ACTIVE) {
            throw new IllegalStateException("Merchant is not active");
        }

        byte[] secret = new byte[32];
        RANDOM.nextBytes(secret);
        UUID keyId = UUID.randomUUID();
        MerchantApiKey key = new MerchantApiKey(keyId, merchantId, sha256(secret), scopes, expiresAt);
        keyRepository.save(key);
        return new IssuedKey(keyId, PREFIX + keyId + "." +
                Base64.getUrlEncoder().withoutPadding().encodeToString(secret));
    }

    /** Invalid, expired and revoked keys all return empty; never reveal which condition failed. */
    @Transactional(readOnly = true)
    public Optional<VerifiedKey> verify(String presented) {
        ParsedKey parsed = parse(presented);
        if (parsed == null) return Optional.empty();

        return keyRepository.findById(parsed.keyId())
                .filter(key -> key.getEnvironment() == MerchantApiKey.Environment.TEST)
                .filter(key -> key.isUsable(Instant.now()))
                .filter(key -> MessageDigest.isEqual(key.getSecretHash(), sha256(parsed.secret())))
                .filter(key -> merchantRepository.findById(key.getMerchantId())
                        .map(merchant -> merchant.getStatus() == MerchantStatus.ACTIVE)
                        .orElse(false))
                .map(key -> new VerifiedKey(key.getKeyId(), key.getMerchantId(), key.getScopes()));
    }

    /** For future authenticated owner/admin flows; there is intentionally no public HTTP route yet. */
    @Transactional
    public void revoke(UUID keyId) {
        MerchantApiKey key = keyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("API key not found"));
        key.revoke();
    }

    private static ParsedKey parse(String presented) {
        if (presented == null || presented.length() > 128 || !presented.startsWith(PREFIX)) return null;
        int dot = presented.indexOf('.', PREFIX.length());
        if (dot < 0 || dot != presented.lastIndexOf('.')) return null;
        String idString = presented.substring(PREFIX.length(), dot);
        String encoded = presented.substring(dot + 1);
        try {
            UUID id = UUID.fromString(idString);
            if (!id.toString().equals(idString) || encoded.length() != 43) return null;
            byte[] secret = Base64.getUrlDecoder().decode(encoded);
            if (secret.length != 32 ||
                    !Base64.getUrlEncoder().withoutPadding().encodeToString(secret).equals(encoded)) return null;
            return new ParsedKey(id, secret);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static byte[] sha256(byte[] secret) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(secret);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private record ParsedKey(UUID keyId, byte[] secret) { }

    public record VerifiedKey(UUID keyId, UUID merchantId, Set<ApiKeyScope> scopes) {
        public VerifiedKey { scopes = Set.copyOf(scopes); }
    }

    public record IssuedKey(UUID keyId, String rawKey) {
        @Override public String toString() { return "IssuedKey[keyId=" + keyId + ", rawKey=[REDACTED]]"; }
    }
}
