package com.exemplo.modelo;

import jakarta.persistence.Entity;

@Entity
public class ProdutoEletronico extends Produto {
    private Integer voltagem;
    
    public ProdutoEletronico() {}

    public Integer getVoltagem() { return voltagem; }
    public void setVoltagem(Integer voltagem) { this.voltagem = voltagem; }
    
    // Métodos herdados do Produto: cadastrar, excluir, alterar, consultar
    @Override
    public void cadastrar() {
        System.out.println("Produto Eletrônico cadastrado: " + this.getNome());
    }
}
