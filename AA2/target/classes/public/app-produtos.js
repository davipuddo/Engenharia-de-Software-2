const $$ = (s) => document.querySelector(s);
let editId = null;
const extraInput = $$("#extra");
const extraLabelText = $$("#extraLabelText");

const moneyBr = (v) => `R$ ${Number(v || 0).toFixed(2)}`;

async function req(url, opts = {}) {
  const res = await fetch(url, opts);
  if (!res.ok) throw new Error(await res.text());
  return res.headers.get("content-type")?.includes("application/json") ? res.json() : res.text();
}

function limparForm() {
  editId = null;
  $$("#tituloForm").textContent = "Cadastrar Produto";
  $$("#formProduto").reset();
  $$("#btnCancelarEdicao").hidden = true;
}

function atualizarLabelExtra() {
  const tipo = $$("#tipo").value;
  if (tipo === "Eletrônico") {
    extraLabelText.textContent = "Voltagem (V)";
    extraInput.type = "number";
    extraInput.min = "0";
    extraInput.step = "1";
    extraInput.placeholder = "Ex: 220";
  } else {
    extraLabelText.textContent = "Data de validade";
    extraInput.type = "date";
    extraInput.placeholder = "";
  }
}

async function carregarProdutos() {
  const produtos = await req("/api/produtos");
  $$("#tbProdutos").innerHTML = produtos.map(p => `
    <tr>
      <td>${p.id}</td>
      <td>${p.tipo}</td>
      <td>${p.nome}</td>
      <td>${moneyBr(p.preco)}</td>
      <td>${p.estoque}</td>
      <td>${p.extra || ""}</td>
      <td>
        <button class="btn btn-primary" onclick='editar(${JSON.stringify(p).replace(/'/g, "&apos;")})'>Editar</button>
        <button class="btn btn-danger" onclick="excluirProduto(${p.id})">Excluir</button>
      </td>
    </tr>
  `).join("");
}

function editar(p) {
  editId = p.id;
  $$("#tituloForm").textContent = `Editar Produto #${p.id}`;
  $$("#nome").value = p.nome;
  $$("#tipo").value = p.tipo;
  $$("#preco").value = p.preco;
  $$("#estoque").value = p.estoque;
  atualizarLabelExtra();
  if (p.tipo === "Eletrônico") {
    extraInput.value = String(p.extra || "").replace(/[^\d]/g, "");
  } else {
    extraInput.value = p.extra || "";
  }
  $$("#btnCancelarEdicao").hidden = false;
}
window.editar = editar;

async function excluirProduto(id) {
  if (!confirm("Excluir produto?")) return;
  try {
    await req(`/api/produtos/${id}`, { method: "DELETE" });
    await carregarProdutos();
  } catch (e) { alert(e.message); }
}
window.excluirProduto = excluirProduto;

$$("#tipo").addEventListener("change", () => {
  atualizarLabelExtra();
  if (editId) {
    extraInput.value = "";
  }
});

$$("#btnCancelarEdicao").addEventListener("click", () => limparForm());

$$("#formProduto").addEventListener("submit", async (ev) => {
  ev.preventDefault();

  const payload = {
    nome: $$("#nome").value,
    tipo: $$("#tipo").value,
    preco: Number($$("#preco").value),
    estoque: Number($$("#estoque").value),
    extra: $$("#extra").value
  };

  try {
    if (editId) {
      await req(`/api/produtos/${editId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    } else {
      await req("/api/produtos", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
    }
    limparForm();
    await carregarProdutos();
  } catch (e) { alert(e.message); }
});

(async function init() {
  atualizarLabelExtra();
  await carregarProdutos();
})();
