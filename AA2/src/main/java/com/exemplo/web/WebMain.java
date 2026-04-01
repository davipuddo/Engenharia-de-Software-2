package com.exemplo.web;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Session;

import com.exemplo.modelo.Item;
import com.exemplo.modelo.Pedido;
import com.exemplo.modelo.Produto;
import com.exemplo.modelo.ProdutoEletronico;
import com.exemplo.modelo.ProdutoPerecivel;
import com.exemplo.util.HibernateUtil;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;

public class WebMain {
    
    // DTOs para evitar dependência cíclica ao serializar/desserializar via Jackson JSON
    public record ProdutoDTO(Long id, String tipo, String nome, Double preco, Integer estoque, String extra) {}
    public record PedidoDTO(Long id, String data, Double valorTotal, int qtdItens) {}
    public record ItemDTO(Long id, Long produtoId, String produtoNome, Integer qtd, Double valorItem) {}
    public record ProdutoInput(String tipo, String nome, Double preco, Integer estoque, String extra) {}
    public record ItemInput(Long produtoId, Integer qtde) {}

    public static void main(String[] args) {
        
        System.out.println("Inicializando banco de dados...");
        try {
            HibernateUtil.getSessionFactory(); // Força carga inicial do Hibernate
        } catch (Exception e) {
            System.err.println("Erro ao conectar no banco de dados. " + e.getMessage());
            // A execução continua, o erro será mostrado na tentativa de acesso as rotas se não for ajustado.
        }
        
        System.out.println("Iniciando servidor web na porta 8080...");
        Javalin app = Javalin.create(config -> {
            // Serve a pasta 'public' (que está em src/main/resources/public) como conteúdo estático
            config.staticFiles.add("/public", Location.CLASSPATH);
        }).start(8080);

        // ==== ENDPOINTS DE PRODUTOS ====
        
        // Listar produtos
        app.get("/api/produtos", ctx -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                List<Produto> produtos = session.createQuery("from Produto", Produto.class).list();
                
                List<ProdutoDTO> dtos = produtos.stream().map(p -> {
                    String tipo = "Produto";
                    String extra = "";
                    if (p instanceof ProdutoEletronico) {
                        tipo = "Eletrônico";
                        extra = ((ProdutoEletronico) p).getVoltagem() + "V";
                    } else if (p instanceof ProdutoPerecivel) {
                        tipo = "Perecível";
                        extra = ((ProdutoPerecivel) p).getDataValidade() != null ? 
                                ((ProdutoPerecivel) p).getDataValidade().toString() : "";
                    }
                    return new ProdutoDTO(p.getId(), tipo, p.getNome(), p.getPreco(), p.getEstoque(), extra);
                }).collect(Collectors.toList());
                
                ctx.json(dtos);
            }
        });

        // Criar produto
        app.post("/api/produtos", ctx -> {
            ProdutoInput input = ctx.bodyAsClass(ProdutoInput.class);
            
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                Produto p;
                
                if ("Eletrônico".equalsIgnoreCase(input.tipo())) {
                    ProdutoEletronico pe = new ProdutoEletronico();
                    pe.setVoltagem(Integer.parseInt(input.extra()));
                    p = pe;
                } else {
                    ProdutoPerecivel pp = new ProdutoPerecivel();
                    pp.setDataValidade(LocalDate.parse(input.extra()));
                    p = pp;
                }
                
                p.setNome(input.nome());
                p.setPreco(input.preco());
                p.setEstoque(input.estoque());

                session.persist(p);
                session.getTransaction().commit();
                
                ctx.status(201).result("Produto criado com sucesso");
            } catch (Exception e) {
                ctx.status(400).result("Erro ao criar produto: " + e.getMessage());
            }
        });

        // Atualizar produto (Alterar)
        app.put("/api/produtos/{id}", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            ProdutoInput input = ctx.bodyAsClass(ProdutoInput.class);
            
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                Produto p = session.get(Produto.class, id);
                if (p == null) {
                    ctx.status(404).result("Produto não encontrado");
                    session.getTransaction().rollback();
                    return;
                }
                
                p.setNome(input.nome());
                p.setPreco(input.preco());
                p.setEstoque(input.estoque());

                if (p instanceof ProdutoEletronico && "Eletrônico".equalsIgnoreCase(input.tipo())) {
                    ((ProdutoEletronico) p).setVoltagem(Integer.parseInt(input.extra()));
                } else if (p instanceof ProdutoPerecivel && "Perecível".equalsIgnoreCase(input.tipo())) {
                    ((ProdutoPerecivel) p).setDataValidade(LocalDate.parse(input.extra()));
                } else if (!p.getClass().getSimpleName().replace("Produto", "").equalsIgnoreCase(input.tipo().replace("ônico", "onico").replace("ível", "ivel"))) {
                    ctx.status(400).result("Não é possível alterar a categoria (Eletrônico/Perecível) de um produto existente.");
                    session.getTransaction().rollback();
                    return;
                }

                session.merge(p);
                session.getTransaction().commit();
                ctx.status(200).result("Produto atualizado com sucesso");
            } catch (Exception e) {
                ctx.status(400).result("Erro ao atualizar produto: " + e.getMessage());
            }
        });

        // Excluir produto
        app.delete("/api/produtos/{id}", ctx -> {
            Long id = Long.parseLong(ctx.pathParam("id"));
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                Produto p = session.get(Produto.class, id);
                if (p != null) {
                    session.remove(p);
                    session.getTransaction().commit();
                    ctx.status(200).result("Produto excluído com sucesso");
                } else {
                    ctx.status(404).result("Produto não encontrado");
                    session.getTransaction().rollback();
                }
            } catch (Exception e) {
                ctx.status(400).result("Erro ao excluir produto. Ele pode estar vinculado a um pedido existente. " + e.getMessage());
            }
        });

        // ==== ENDPOINTS DE PEDIDOS ====
        
        // Listar pedidos
        app.get("/api/pedidos", ctx -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                List<Pedido> pedidos = session.createQuery("from Pedido", Pedido.class).list();
                List<PedidoDTO> dtos = pedidos.stream().map(p -> 
                    new PedidoDTO(p.getId(), p.getData() != null ? p.getData().toString() : "", p.getValorTotal() != null ? p.getValorTotal() : 0.0, p.getItens().size())
                ).collect(Collectors.toList());
                
                ctx.json(dtos);
            }
        });

        // Criar um pedido vazio
        app.post("/api/pedidos", ctx -> {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();
                
                Pedido pedido = new Pedido();
                pedido.setData(LocalDate.now());
                pedido.setValorTotal(0.0);

                session.persist(pedido);
                session.getTransaction().commit();
                
                ctx.status(201).result("Pedido criado com sucesso");
            } catch (Exception e) {
                ctx.status(500).result("Erro ao criar pedido: " + e.getMessage());
            }
        });
        
        // ==== ENDPOINTS DE ITENS ====
        
        // Listar itens de um pedido
        app.get("/api/pedidos/{id}/itens", ctx -> {
            Long pedidoId = Long.parseLong(ctx.pathParam("id"));
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Pedido pedido = session.get(Pedido.class, pedidoId);
                if(pedido == null) {
                    ctx.status(404).result("Pedido não encontrado.");
                    return;
                }
                
                List<ItemDTO> dtos = pedido.getItens().stream().map(item -> 
                    new ItemDTO(item.getCodigoItem(), item.getProduto().getId(), item.getProduto().getNome(), item.getQtde(), item.getValorItem())
                ).collect(Collectors.toList());
                
                ctx.json(dtos);
            }
        });

        // Adicionar item a um pedido
        app.post("/api/pedidos/{id}/itens", ctx -> {
            Long pedidoId = Long.parseLong(ctx.pathParam("id"));
            ItemInput input = ctx.bodyAsClass(ItemInput.class);

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();

                if (input == null || input.produtoId() == null || input.qtde() == null || input.qtde() <= 0) {
                    ctx.status(400).result("Quantidade inválida.");
                    session.getTransaction().rollback();
                    return;
                }

                Pedido pedido = session.get(Pedido.class, pedidoId);
                Produto produto = session.get(Produto.class, input.produtoId());

                if (pedido == null || produto == null) {
                    ctx.status(404).result("Pedido ou Produto não encontrado.");
                    session.getTransaction().rollback();
                    return;
                }

                int estoqueAtual = produto.getEstoque() != null ? produto.getEstoque() : 0;
                if (input.qtde() > estoqueAtual) {
                    ctx.status(409).result("Estoque insuficiente. Disponível: " + estoqueAtual);
                    session.getTransaction().rollback();
                    return;
                }

                Item item = new Item();
                item.setProduto(produto);
                item.setQtde(input.qtde());
                item.setValorItem(produto.getPreco());
                item.setPedido(pedido);

                pedido.getItens().add(item);

                produto.setEstoque(estoqueAtual - input.qtde());
                session.merge(produto);

                double total = pedido.getItens().stream().mapToDouble(i -> i.getValorItem() * i.getQtde()).sum();
                pedido.setValorTotal(total);

                session.merge(pedido);
                session.getTransaction().commit();

                ctx.status(201).result("Item adicionado com sucesso.");
            } catch (Exception e) {
                ctx.status(500).result("Erro ao adicionar item: " + e.getMessage());
            }
        });

        // Remover item
        app.delete("/api/itens/{id}", ctx -> {
            Long itemId = Long.parseLong(ctx.pathParam("id"));
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                session.beginTransaction();

                Item item = session.get(Item.class, itemId);
                if (item == null) {
                    ctx.status(404).result("Item não encontrado.");
                    session.getTransaction().rollback();
                    return;
                }

                Pedido pedido = item.getPedido();
                Produto produto = item.getProduto();
                int estoqueAtual = produto.getEstoque() != null ? produto.getEstoque() : 0;
                int qtdItem = item.getQtde() != null ? item.getQtde() : 0;
                produto.setEstoque(estoqueAtual + qtdItem);
                session.merge(produto);

                pedido.getItens().remove(item);

                double total = pedido.getItens().stream().mapToDouble(i -> i.getValorItem() * i.getQtde()).sum();
                pedido.setValorTotal(total);

                session.remove(item);
                session.merge(pedido);

                session.getTransaction().commit();

                ctx.status(200).result("Item removido com sucesso.");
            } catch (Exception e) {
                ctx.status(500).result("Erro ao remover item: " + e.getMessage());
            }
        });
        
        System.out.println("Servidor web pronto! Acesse http://localhost:8080/ no seu navegador.");
    }
}
