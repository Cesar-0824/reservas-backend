package com.canchas.reservas.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // ✅ Clave secreta segura (mínimo 32 caracteres para HS256)
    private static final String SECRET_KEY = "canchasSegurasClaveSecretaJWT123456!";

    // Tiempo de expiración (ejemplo: 10 horas)
    private static final long EXPIRATION_MS = 10 * 60 * 60 * 1000;

    // 🔑 Devuelve la clave secreta como tipo SecretKey
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // 🔐 Genera un token con el email y el rol
    public String generateToken(String email, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol); // Se guarda como "rol", no "role"

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 📥 Extrae el email (subject) del token
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 🔍 Extrae el rol desde el token
    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("rol", String.class); // key debe coincidir con el nombre que usaste en generateToken
    }

    // ✔️ Verifica si el token es válido (opcional)
    public boolean validateToken(String token, String email) {
        String username = extractUsername(token);
        return username.equals(email) && !isTokenExpired(token);
    }

    // ⌛ Verifica si el token expiró (opcional)
    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}
