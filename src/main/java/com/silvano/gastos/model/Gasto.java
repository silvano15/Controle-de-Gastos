package com.silvano.gastos.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "gastos", indexes = @Index(name = "idx_gastos_data_hora", columnList = "data_hora"))
public class Gasto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(length = 100)
    private String descricao;

    /** Momento exato do registro (salvo em UTC, convertido para o fuso local na exibição). */
    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    public Long getId() { return id; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Instant getDataHora() { return dataHora; }
    public void setDataHora(Instant dataHora) { this.dataHora = dataHora; }
}
