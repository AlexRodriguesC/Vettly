package com.vettly.auth;

import java.time.Duration;
import java.util.Base64;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT settings, bound from {@code vettly.jwt.*}.
 *
 * @param secret     Base64-encoded HMAC key, at least 256 bits (32 bytes) once decoded
 * @param expiration how long an issued access token stays valid
 * @param issuer     value written to and expected in the {@code iss} claim
 */
@ConfigurationProperties(prefix = "vettly.jwt")
public record JwtProperties(String secret, Duration expiration, String issuer) {

    private static final int MIN_KEY_BYTES = 32;

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "vettly.jwt.secret is not set. Provide a Base64-encoded key of at least 32 bytes "
                            + "(e.g. via the VETTLY_JWT_SECRET environment variable).");
        }
        if (expiration == null) {
            expiration = Duration.ofHours(1);
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "vettly";
        }
    }

    public byte[] secretBytes() {
        byte[] key;
        try {
            key = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("vettly.jwt.secret must be valid Base64", e);
        }
        if (key.length < MIN_KEY_BYTES) {
            throw new IllegalStateException(
                    "vettly.jwt.secret must decode to at least " + MIN_KEY_BYTES + " bytes for HS256");
        }
        return key;
    }
}
