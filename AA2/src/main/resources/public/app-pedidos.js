const $ = (s) => document.querySelector(s);
const estoqueHint = $("#stockHint");
let pedidoSelecionado = null;
let produtosCache = [];

const money = (v) => `R$ ${Number(v || 0).toFixed(2)}`;

async function api(url, opts = {}) {
  const res = await fetch(url, opts);
  if (!res.ok) throw new Error(await res.text());
  return res.headers.get("content-type")?.includes("application/json") ? res.json() : res.text();
}

async function carregarProdutos() {
  produtosCache = await api("/api/produtos");
  const sel = $("#produtoId");
  sel.innerHTML = `<option value="">Selecione...</option>` + produtosCache
    .map(p => `<option value="${p.id}">${p.id} - ${p.nome} (Estoque: ${p.estoque})</option>`)
    .join("");
  atualizarHintEstoque();
}

function atualizarHintEstoque() {
  const produtoId = $("#produtoId").value;
  const produto = produtosCache.find(p => String(p.id) === produtoId);
  estoqueHint.textContent = produto ? `Estoque disponível: ${produto.estoque ?? 0}` : "";
}

$("#produtoId").addEventListener("change", atualizarHintEstoque);

async function carregarPedidos() {
  const pedidos = await api("/api/pedidos");
  $("#pedidoId").innerHTML = `<option value="">Selecione...</option>` + pedidos
    .map(p => `<option value="${p.id}">#${p.id} - ${p.data}</option>`).join("");

  $("#tbPedidos").innerHTML = pedidos.map(p => `
    <tr>
      <td>${p.id}</td>
      <td>${p.data}</td>
      <td>${money(p.valorTotal)}</td>
      <td><button class="btn btn-primary" onclick="verItens(${p.id})">Ver Itens</button></td>
    </tr>
  `).join("");
}

async function verItens(id) {
  pedidoSelecionado = id;
  const itens = await api(`/api/pedidos/${id}/itens`);
  $("#tbItens").innerHTML = itens.map(i => `
    <tr>
      <td>${i.id}</td>
      <td>${i.produtoNome}</td>
      <td>${i.qtd}</td>
      <td>${money(i.valorItem)}</td>
      <td><button class="btn btn-danger" onclick="removerItem(${i.id})">Remover</button></td>
    </tr>
  `).join("");
}
window.verItens = verItens;

async function removerItem(itemId) {
  if (!confirm("Remover item?")) return;
  try {
    await api(`/api/itens/${itemId}`, { method: "DELETE" });
    await carregarProdutos();
    await carregarPedidos();
    if (pedidoSelecionado) await verItens(pedidoSelecionado);
  } catch (e) { alert(e.message); }
}
window.removerItem = removerItem;

$("#btnCriarPedido").addEventListener("click", async () => {
  try {
    await api("/api/pedidos", { method: "POST" });
    await carregarPedidos();
    alert("Pedido criado.");
  } catch (e) { alert(e.message); }
});

$("#formAddItem").addEventListener("submit", async (ev) => {
  ev.preventDefault();
  const pedidoId = $("#pedidoId").value;
  const produtoId = $("#produtoId").value;
  const qtde = Number($("#qtde").value);

  if (!pedidoId || !produtoId || qtde <= 0) return alert("Preencha os campos corretamente.");

  const produto = produtosCache.find(p => String(p.id) === produtoId);
  if (!produto) return alert("Produto inválido.");
  if (qtde > (produto.estoque ?? 0)) {
    return alert(`Estoque insuficiente. Disponível: ${produto.estoque ?? 0}`);
  }

  try {
    await api(`/api/pedidos/${pedidoId}/itens`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ produtoId: Number(produtoId), qtde })
    });
    pedidoSelecionado = Number(pedidoId);
    await carregarProdutos();
    await carregarPedidos();
    await verItens(pedidoSelecionado);
    alert("Item adicionado.");
  } catch (e) { alert(e.message); }
});

(async function init() {
  await carregarProdutos();
  await carregarPedidos();
})();
