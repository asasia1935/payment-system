# API Key authentication v1 (implementation branch)

## Scope

- Spring Security in the **existing monolith**, no Go Identity, Gateway, Kafka or Kubernetes needed.
- \`POST /api/payments\` requires \`PAYMENT_CREATE\`.
- \`GET /api/payments/{paymentId}\` requires \`PAYMENT_READ\`.
- \`merchantId\` comes **only** from verified API Key principal, never from a JSON request field.
- Payment lookup is constrained by **both paymentId and authenticated merchantId**.
- Any other routes, including existing merchant registration and lookup, are **deny-all** while end-user account authentication is unfinished. This changes access for those routes intentionally and prevents accidental anonymous merchant registration.
- No public API-Key issuance/revocation endpoint until authenticated merchant ownership can be enforced.

## Key format & storage

\`sk_test_{uuid}.{base64url(32 secure random bytes)}\`

\`merchant_api_keys\` stores UUID, merchant UUID, \`SHA-256(secret)\` (32 bytes), TEST environment, ACTIVE/REVOKED state, time metadata; \`merchant_api_key_scopes\` contains permitted operations. Cleartext key is returned once by \`MerchantApiKeyService.issueTestKey\`, which is internal-only at this stage. Secret should never be persisted or logged.

Verification parses format -> lookup by key UUID -> check ACTIVE, expiry, secret hash (constant-time comparison) and merchant ACTIVE status -> construct a Spring Security principal scoped to the merchant. Missing/malformed/revoked key: 401; missing scope: 403.

## Verification

Run on your local setup with PostgreSQL from \`compose.yaml\`:

\`\`\`powershell
docker compose up -d
.\gradlew.bat test
\`\`\`

Test coverage authored:
- Generation, verification and revocation logic (Mockito)
- 401 for missing or invalid key
- 403 for valid read-only key attempting to create a payment
- Merchant principal taking precedence over forged merchantId request content
- 400 for invalid payment request data
- Merchant-scoped GET in the MVC authentication path

**Status:** Source and tests have been authored on this feature branch. CI/local execution has NOT been verified by the assistant; run the command above and review results before merging. This is **not yet complete production functionality**.

## Next steps

1. Run tests on a machine with Gradle dependency access and review failures.
2. Add database-backed integration tests including Merchant A unable to access Merchant B's payment.
3. Add a local-only key provisioning mechanism (or use a test fixture); do not publish an unauthenticated issuance endpoint.
4. Build account authentication, merchant membership and owner-approved key issuance/revocation.
5. Verify secret redaction, HTTPS, rate limiting, audit log and key lifecycle before public exposure.
