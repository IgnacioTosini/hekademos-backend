package com.hekademos.hekademos_backend.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.hekademos.hekademos_backend.entities.User;
import com.hekademos.hekademos_backend.services.IUserService;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.List;

@Service
public class JwtService {

    @Value("${app.jwt-secret}")
    private String secret;

    @Value("${app.jwt-exp-minutes}")
    private long expMinutes;

    // ==========================
    // 🔹 GENERAR TOKEN
    // ==========================
    @Autowired
    private IUserService userService;

    @Transactional
    public String generateToken(String email, String name, String picture) {
        try {
            Instant now = Instant.now();
            User user = userService.getUserByEmail(email);
            List<String> roles = new java.util.ArrayList<>();
            if (user != null && user.getRoles() != null) {
                user.getRoles().size();
                roles = user.getRoles().stream().map(r -> r.getName()).toList();
            }

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(email)
                    .claim("name", name)
                    .claim("picture", picture)
                    .claim("authorities", roles)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(expMinutes * 60)))
                    .build();

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new MACSigner(secret.getBytes()));

            return jwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Error generating JWT", e);
        }
    }

    // ==========================
    // 🔹 VALIDAR TOKEN
    // ==========================
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret.getBytes());

            boolean valid = signedJWT.verify(verifier);

            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return valid && expirationTime.after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    // ==========================
    // 🔹 OBTENER CLAIMS
    // ==========================
    public Map<String, Object> getClaims(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();
            return claims.getClaims();
        } catch (ParseException e) {
            throw new RuntimeException("Error parsing token claims", e);
        }
    }

    // ==========================
    // 🔹 OBTENER EMAIL DEL TOKEN
    // ==========================
    public String getEmail(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Error getting subject (email) from token", e);
        }
    }
}
