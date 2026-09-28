package com.silvano.gastos.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/** Gera e valida o token que identifica o usuário logado (substitui o antigo PIN único). */
@Service
public class JwtService {

    private final SecretKey chave;
    private final long expiracaoDias;

    public JwtService(@Value("${app.jwt-secret}") String segredo,
                       @Value("${app.jwt-expiracao-dias:90}") long expiracaoDias) {
        if (segredo == null || segredo.isBlank()) {
            throw new IllegalStateException("Defina a variável de ambiente JWT_SECRET (mínimo 32 caracteres)");
        }
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoDias = expiracaoDias;
    }

    public String gerar(Long usuarioId) {
        Instant agora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuarioId))
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoDias, ChronoUnit.DAYS)))
                .signWith(chave)
                .compact();
    }

    /** Retorna o id do usuário se o token for válido, ou null caso contrário. */
    public Long validar(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
