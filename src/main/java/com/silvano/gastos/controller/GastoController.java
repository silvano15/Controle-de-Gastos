package com.silvano.gastos.controller;

import com.silvano.gastos.dto.GastoRequest;
import com.silvano.gastos.dto.GastoResponse;
import com.silvano.gastos.dto.HistoricoResponse;
import com.silvano.gastos.service.GastoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/gastos")
public class GastoController {

    private final GastoService service;

    public GastoController(GastoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GastoResponse registrar(@Valid @RequestBody GastoRequest req) {
        return service.registrar(req);
    }

    /** periodo: MES (padrão), 7D, 15D, 30D, 3M, 6M, 12M */
    @GetMapping
    public HistoricoResponse historico(@RequestParam(defaultValue = "MES") String periodo) {
        return service.historico(periodo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> erroValidacao(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst().map(e -> e.getDefaultMessage()).orElse("Dados inválidos");
        return ResponseEntity.badRequest().body(Map.of("erro", msg));
    }
}
