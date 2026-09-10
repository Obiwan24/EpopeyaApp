package com.example.epopeyaap.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JwtUtil
 * -------
 * ANTES: clase vacía. Sin esto no existe ninguna forma de crear ni de leer
 * el token que auth.js guarda en localStorage("token") y que api.js manda
 * en la cabecera "Authorization: Bearer ...".
 *
 * Responsabilidades:
 *  - generarToken(username, rol): crea el JWT al hacer login correctamente.
 *  - extraerUsername(token) / extraerRol(token): leen el contenido del token
 *    en cada petición (lo usa JwtAuthFilter).
 *  - validarToken(token, username): comprueba firma + expiración.
 *
 * La clave secreta (jwt.secret) se define en application.properties y NUNCA
 * debe subirse a un repositorio público; en Render/Railway se pasa como
 * variable de entorno, igual que ya haces con DB_USERNAME/DB_PASSWORD.
 * Debe tener al menos 32 caracteres porque el algoritmo HS256 exige una
 * clave de 256 bits como mínimo.
 */
@Component
public class JwtUtil {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secreto,
            @Value("${jwt.expiration-ms:86400000}") long expiracionMs // 24h por defecto
    ) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    /** Crea un token firmado que incluye el username (subject) y el rol como claim extra. */
    public String generarToken(String username, String rol, Long usuarioId) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .claim("usuarioId", usuarioId)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(clave)
                .compact();
    }

    public String extraerUsername(String token) {
        return parsearClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return parsearClaims(token).get("rol", String.class);
    }

    /** true si el token está bien firmado, no ha caducado y pertenece al username indicado. */
    public boolean validarToken(String token, String username) {
        try {
            Claims claims = parsearClaims(token);
            boolean mismoUsuario = claims.getSubject().equals(username);
            boolean noCaducado = claims.getExpiration().after(new Date());
            return mismoUsuario && noCaducado;
        } catch (Exception e) {
            // Firma inválida, token manipulado, expirado con error de parseo, etc.
            return false;
        }
    }

    private Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}