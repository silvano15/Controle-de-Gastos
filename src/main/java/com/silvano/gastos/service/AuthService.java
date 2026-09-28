package com.silvano.gastos.service;

import com.silvano.gastos.dto.*;
import com.silvano.gastos.model.Usuario;
import com.silvano.gastos.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    public AuthService(UsuarioRepository repository, JwtService jwtService, EmailService emailService) {
        this.repository = repository;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public AuthResponse cadastrar(CadastroRequest req) {
        String email = req.email().trim().toLowerCase();
        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esse e-mail já tem uma conta");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(req.nome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(encoder.encode(req.senha()));
        usuario.setCriadoEm(Instant.now());
        usuario = repository.save(usuario);
        return new AuthResponse(jwtService.gerar(usuario.getId()), usuario.getNome(), usuario.getEmail());
    }

    public AuthResponse login(LoginRequest req) {
        Usuario usuario = repository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));
        if (!encoder.matches(req.senha(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos");
        }
        return new AuthResponse(jwtService.gerar(usuario.getId()), usuario.getNome(), usuario.getEmail());
    }

    /** Sempre "funciona" do ponto de vista do chamador, mesmo se o e-mail não existir (evita revelar quem tem conta). */
    public void esqueciSenha(String email) {
        repository.findByEmail(email.trim().toLowerCase()).ifPresent(usuario -> {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            usuario.setResetToken(token);
            usuario.setResetTokenExpira(Instant.now().plus(1, ChronoUnit.HOURS));
            repository.save(usuario);

            emailService.enviarResetSenha(usuario.getEmail(), usuario.getNome(), token);
        });
    }

    public void resetarSenha(ResetarSenhaRequest req) {
        Usuario usuario = repository.findByResetToken(req.token())
                .filter(u -> u.getResetTokenExpira() != null && u.getResetTokenExpira().isAfter(Instant.now()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link inválido ou expirado"));
        usuario.setSenhaHash(encoder.encode(req.novaSenha()));
        usuario.setResetToken(null);
        usuario.setResetTokenExpira(null);
        repository.save(usuario);
    }

    public UsuarioResponse buscar(Long usuarioId) {
        Usuario usuario = repository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new UsuarioResponse(usuario.getNome(), usuario.getEmail());
    }
}
