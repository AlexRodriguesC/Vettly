package com.vettly.auth;

/**
 * Returned on successful register or login.
 *
 * @param accessToken the signed JWT to send as {@code Authorization: Bearer <token>}
 * @param tokenType   always {@code Bearer}
 * @param expiresIn   seconds until the token expires
 */
public record AuthResponse(String accessToken, String tokenType, long expiresIn) {
}
