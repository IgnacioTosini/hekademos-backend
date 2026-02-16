package com.hekademos.hekademos_backend.security;

import javax.crypto.SecretKey;

import io.jsonwebtoken.security.Keys;

public class TokenJwtConfig {
    // Clave fija para que los tokens sigan siendo válidos después de reiniciar el servidor
    private static final String SECRET_STRING = "miClaveSecretaSuperSeguraParaJWTQueDebeSerMuyLargaYCompleja123456789";
    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    public static final String PREFIX_TOKEN = "Bearer ";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "application/json";
}
