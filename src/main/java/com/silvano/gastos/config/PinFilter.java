package com.silvano.gastos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Proteção simples: como o site fica público na internet, toda chamada em /api
 * precisa enviar o cabeçalho "X-Pin" igual à variável de ambiente APP_PIN.
 * Se APP_PIN estiver vazia, a proteção fica desligada.
 */
@Component
public class PinFilter extends OncePerRequestFilter {

    private final String pin;

    public PinFilter(@Value("${app.pin:}") String pin) {
        this.pin = pin == null ? "" : pin.trim();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return pin.isEmpty() || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String enviado = req.getHeader("X-Pin");
        boolean ok = enviado != null && MessageDigest.isEqual(
                enviado.getBytes(StandardCharsets.UTF_8), pin.getBytes(StandardCharsets.UTF_8));
        if (!ok) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"erro\":\"PIN inválido\"}");
            return;
        }
        chain.doFilter(req, res);
    }
}
