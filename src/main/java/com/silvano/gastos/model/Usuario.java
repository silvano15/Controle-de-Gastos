package com.silvano.gastos.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "reset_token", length = 64)
    private String resetToken;

    @Column(name = "reset_token_expira")
    private Instant resetTokenExpira;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public Instant getResetTokenExpira() { return resetTokenExpira; }
    public void setResetTokenExpira(Instant resetTokenExpira) { this.resetTokenExpira = resetTokenExpira; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
}
