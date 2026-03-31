package com.exemplo.modelo;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nome;
    private Double preco;
    private Integer estoque;
    
    // Construtor vazio (requerido pelo JPA)
    public Produto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }

    public Integer getEstoque() { return estoque; }
    public void setEstoque(Integer estoque) { this.estoque = estoque; }

    // Métodos indicados no diagrama (Apenas protótipo sem implementação completa de regras de negócios)
    public void cadastrar() { System.out.println("Produto cadastrado: " + this.nome); }
    public void excluir() { System.out.println("Produto excluído: " + this.nome); }
    public void alterar() { System.out.println("Produto alterado: " + this.nome); }
    public void consultar() { System.out.println("Consultando Produto: " + this.nome); }
}
