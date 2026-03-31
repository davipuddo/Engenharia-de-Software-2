package com.exemplo.modelo;

import jakarta.persistence.Entity;
import java.time.LocalDate;

@Entity
public class ProdutoPerecivel extends Produto {
    private LocalDate dataValidade;
    
    public ProdutoPerecivel() {}

    public LocalDate getDataValidade() { return dataValidade; }
    public void setDataValidade(LocalDate dataValidade) { this.dataValidade = dataValidade; }
    
    // Métodos herdados do Produto: cadastrar, excluir, alterar, consultar
    @Override
    public void cadastrar() {
        System.out.println("Produto Perecível cadastrado: " + this.getNome());
    }
}
