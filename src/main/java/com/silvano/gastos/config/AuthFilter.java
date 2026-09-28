package com.silvano.gastos.config;

import com.silvano.gastos.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Protege as rotas de /api (exceto /api/auth/**) exigindo um token JWT válido
 * no cabeçalho "Authorization: Bearer ...". O id do usuário logado fica disponível
 * nos controllers via @RequestAttribute Long usuarioId.
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public AuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    private static final Set<String> ROTAS_PUBLICAS = Set.of(
            "/api/auth/cadastro", "/api/auth/login", "/api/auth/esqueci-senha", "/api/auth/resetar-senha");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/") || ROTAS_PUBLICAS.contains(uri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
        Long usuarioId = token == null ? null : jwtService.validar(token);

        if (usuarioId == null) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"erro\":\"Sessão inválida ou expirada, faça login novamente\"}");
            return;
        }
        req.setAttribute("usuarioId", usuarioId);
        chain.doFilter(req, res);
    }
}
