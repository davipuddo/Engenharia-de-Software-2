package com.exemplo.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codigoItem;
    
    private Integer qtde;
    private Double valorItem;
    
    @ManyToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;
    
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;
    
    public Item() {}

    public Long getCodigoItem() { return codigoItem; }
    public void setCodigoItem(Long codigoItem) { this.codigoItem = codigoItem; }

    public Integer getQtde() { return qtde; }
    public void setQtde(Integer qtde) { this.qtde = qtde; }

    public Double getValorItem() { return valorItem; }
    public void setValorItem(Double valorItem) { this.valorItem = valorItem; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    // Métodos do diagrama
    public void adicionar() { System.out.println("Item adicionado: " + this.codigoItem); }
    public void remover() { System.out.println("Item removido: " + this.codigoItem); }
}
