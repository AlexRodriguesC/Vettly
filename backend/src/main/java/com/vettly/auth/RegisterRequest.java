package com.vettly.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 255) String email,
        // Upper bound keeps us under BCrypt's 72-byte input limit for ASCII passwords.
        @NotBlank @Size(min = 8, max = 72) String password) {

    public RegisterRequest {
        // Trim before validation runs so a pasted address with stray spaces is accepted.
        email = email == null ? null : email.trim();
    }
}
