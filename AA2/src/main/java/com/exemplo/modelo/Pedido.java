package com.exemplo.modelo;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate data;
    private Double valorTotal;
    
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> itens = new ArrayList<>();
    
    public Pedido() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Double getValorTotal() { return valorTotal; }
    public void setValorTotal(Double valorTotal) { this.valorTotal = valorTotal; }

    public List<Item> getItens() { return itens; }
    public void setItens(List<Item> itens) { this.itens = itens; }

    // Métodos do diagrama
    public void consultar() { System.out.println("Consultando pedido ID: " + this.id); }
    public void cadastrar() { System.out.println("Cadastrando pedido com total: " + this.valorTotal); }
}
