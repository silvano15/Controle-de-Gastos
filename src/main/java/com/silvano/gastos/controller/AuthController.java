package com.silvano.gastos.controller;

import com.silvano.gastos.dto.*;
import com.silvano.gastos.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse cadastro(@Valid @RequestBody CadastroRequest req) {
        return service.cadastrar(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/esqueci-senha")
    public void esqueciSenha(@Valid @RequestBody EsqueciSenhaRequest req) {
        service.esqueciSenha(req.email());
    }

    @PostMapping("/resetar-senha")
    public void resetarSenha(@Valid @RequestBody ResetarSenhaRequest req) {
        service.resetarSenha(req);
    }

    @GetMapping("/eu")
    public UsuarioResponse eu(@RequestAttribute Long usuarioId) {
        return service.buscar(usuarioId);
    }
}
