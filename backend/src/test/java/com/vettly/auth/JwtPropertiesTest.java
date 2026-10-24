package com.vettly.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Base64;

import org.junit.jupiter.api.Test;

class JwtPropertiesTest {

    @Test
    void missingSecretFailsFast() {
        assertThatThrownBy(() -> new JwtProperties("", Duration.ofHours(1), "vettly"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("VETTLY_JWT_SECRET");
    }

    @Test
    void tooShortKeyIsRejected() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        JwtProperties properties = new JwtProperties(shortKey, Duration.ofHours(1), "vettly");

        assertThatThrownBy(properties::secretBytes)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void defaultsAreAppliedWhenOmitted() {
        String key = Base64.getEncoder().encodeToString(new byte[32]);
        JwtProperties properties = new JwtProperties(key, null, null);

        assertThat(properties.expiration()).isEqualTo(Duration.ofHours(1));
        assertThat(properties.issuer()).isEqualTo("vettly");
        assertThat(properties.secretBytes()).hasSize(32);
    }
}
