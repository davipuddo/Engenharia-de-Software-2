let produtosGlobais = [];
let pedidosGlobais = [];
let produtoEditandoId = null;

// Altera o label baseado no tipo de produto
function updateExtraLabel() {
	const tipo = document.getElementById('tipo').value;
	const lbl = document.getElementById('lblExtra');
	const input = document.getElementById('extra');
	if(tipo === 'Eletrônico') {
		lbl.innerText = 'Voltagem (V):';
		input.placeholder = 'Ex: 220';
		input.type = 'number';
	} else {
		lbl.innerText = 'Data de Validade:';
		input.placeholder = 'Ex: yyyy-mm-dd';
		input.type = 'date';
	}
}

// Carrega produtos da API e preenche a tabela e os selects
async function loadProdutos() {
	const res = await fetch('/api/produtos');
	produtosGlobais = await res.json();
	const tbody = document.getElementById('tabelaProdutos');
	const selectProd = document.getElementById('itemProdutoId');
	
	tbody.innerHTML = '';
	selectProd.innerHTML = '<option value="">Selecione um produto...</option>';
	
	produtosGlobais.forEach(p => {
		// Escapar strings para uso no onclick json para evitar problemas com aspas
		const prodJson = encodeURIComponent(JSON.stringify(p));
		
		tbody.innerHTML += `<tr>
			<td>${p.id}</td>
			<td>${p.tipo}</td>
			<td>${p.nome}</td>
			<td>R$ ${p.preco.toFixed(2)}</td>
			<td>${p.estoque}</td>
			<td>${p.extra}</td>
			<td>
				<button class="btn-blue" style="padding: 5px; width: auto; font-size: 0.8em; margin-bottom: 5px;" onclick="editarProduto('${prodJson}')">Alterar</button>
				<button style="background: #dc3545; padding: 5px; width: auto; font-size: 0.8em;" onclick="excluirProduto(${p.id})">Excluir</button>
			</td>
		</tr>`;
		
		selectProd.innerHTML += `<option value="${p.id}">${p.id} - ${p.nome} (R$ ${p.preco})</option>`;
	});
}

function editarProduto(pStr) {
	const p = JSON.parse(decodeURIComponent(pStr));
	produtoEditandoId = p.id;
	
	document.getElementById('nome').value = p.nome;
	document.getElementById('tipo').value = p.tipo;
	document.getElementById('tipo').disabled = true; // Não permite alterar tipo
	document.getElementById('preco').value = p.preco;
	document.getElementById('estoque').value = p.estoque;
	
	updateExtraLabel();
	
	// Format extra data if needed
	let extraVal = p.extra;
	if (p.tipo === 'Eletrônico' && extraVal.endsWith('V')) {
		extraVal = extraVal.slice(0, -1);
	}
	document.getElementById('extra').value = extraVal;
	
	document.getElementById('btnSubmitProduto').innerText = "Atualizar Produto";
	document.getElementById('btnSubmitProduto').classList.add('btn-blue');
	document.getElementById('btnCancelEdit').style.display = 'block';
}

function cancelarEdicao() {
	produtoEditandoId = null;
	document.getElementById('formProduto').reset();
	document.getElementById('tipo').disabled = false;
	updateExtraLabel();
	
	document.getElementById('btnSubmitProduto').innerText = "Cadastrar Produto";
	document.getElementById('btnSubmitProduto').classList.remove('btn-blue');
	document.getElementById('btnCancelEdit').style.display = 'none';
}

async function excluirProduto(id) {
	if(!confirm("Tem certeza que deseja excluir este produto?")) return;
	
	const res = await fetch(`/api/produtos/${id}`, { method: 'DELETE' });
	if(res.ok) {
		alert('Produto excluído com sucesso!');
		if(produtoEditandoId === id) cancelarEdicao();
		loadProdutos();
	} else {
		alert('Erro ao excluir produto: ' + await res.text());
	}
}

// Carrega pedidos da API e preenche a tabela e selects
async function loadPedidos() {
	const res = await fetch('/api/pedidos');
	pedidosGlobais = await res.json();
	const tbody = document.getElementById('tabelaPedidos');
	const selectPedido = document.getElementById('itemPedidoId');
	
	tbody.innerHTML = '';
	selectPedido.innerHTML = '<option value="">Selecione um pedido...</option>';
	
	pedidosGlobais.forEach(p => {
		tbody.innerHTML += `<tr>
			<td>${p.id}</td>
			<td>${p.data}</td>
			<td>R$ ${p.valorTotal.toFixed(2)}</td>
			<td>
				<button class="btn-blue" onclick="verItens(${p.id})" style="padding: 5px; width: auto; font-size: 0.8em;">Ver Itens</button>
			</td>
		</tr>`;
		
		selectPedido.innerHTML += `<option value="${p.id}">Pedido #${p.id}</option>`;
	});
}

// Carrega os itens de um pedido específico
async function verItens(pedidoId) {
	const table = document.getElementById('tabelaItens');
	const tbody = document.getElementById('tabelaItensBody');
	table.style.display = 'table';
	tbody.innerHTML = '<tr><td colspan="5">Carregando...</td></tr>';
	
	const res = await fetch(`/api/pedidos/${pedidoId}/itens`);
	if(res.ok) {
		const itens = await res.json();
		tbody.innerHTML = '';
		if(itens.length === 0) {
			tbody.innerHTML = '<tr><td colspan="5">Nenhum item neste pedido.</td></tr>';
		} else {
			itens.forEach(item => {
				tbody.innerHTML += `<tr>
					<td>${item.id}</td>
					<td>${item.produtoNome}</td>
					<td>${item.qtd}</td>
					<td>R$ ${item.valorItem.toFixed(2)}</td>
					<td>
						<button style="background: #dc3545; padding: 5px; width: auto; font-size: 0.8em;" onclick="removerItem(${item.id})">Remover</button>
					</td>
				</tr>`;
			});
		}
	} else {
		tbody.innerHTML = '<tr><td colspan="5">Erro ao carregar itens.</td></tr>';
	}
}

// Cria pedido vazio
document.getElementById('formPedido').addEventListener('submit', async (e) => {
	e.preventDefault();
	const res = await fetch('/api/pedidos', { method: 'POST' });
	if(res.ok) {
		alert('Pedido vazio criado com sucesso!');
		loadPedidos();
	} else {
		alert('Erro ao criar pedido.');
	}
});

// Adiciona Item ao Pedido
document.getElementById('formItem').addEventListener('submit', async (e) => {
	e.preventDefault();
	const pedidoId = document.getElementById('itemPedidoId').value;
	const produtoId = document.getElementById('itemProdutoId').value;
	const qtd = parseInt(document.getElementById('itemQtd').value);
	
	if(!pedidoId || !produtoId) {
		alert("Selecione pedido e produto.");
		return;
	}

	const payload = { produtoId: parseInt(produtoId), qtde: qtd };
	
	const res = await fetch(`/api/pedidos/${pedidoId}/itens`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(payload)
	});

	if(res.ok) {
		alert('Item adicionado com sucesso!');
		document.getElementById('itemQtd').value = 1;
		loadPedidos();
		verItens(pedidoId);
	} else {
		alert('Erro ao adicionar item: ' + await res.text());
	}
});

// Remove item
async function removerItem(itemId) {
	if(!confirm("Tem certeza que deseja remover este item?")) return;
	
	const res = await fetch(`/api/itens/${itemId}`, { method: 'DELETE' });
	if(res.ok) {
		alert('Item removido!');
		loadPedidos();
		// Fecha a tabela de itens já que não sabemos de qual pedido era facilmente do lado do client sem mais lógica
		document.getElementById('tabelaItens').style.display = 'none'; 
	} else {
		alert('Erro ao remover item.');
	}
}

// Envia o formulário para criar ou alterar um produto
document.getElementById('formProduto').addEventListener('submit', async (e) => {
	e.preventDefault();
	const payload = {
		nome: document.getElementById('nome').value,
		tipo: document.getElementById('tipo').value,
		preco: parseFloat(document.getElementById('preco').value),
		estoque: parseInt(document.getElementById('estoque').value),
		extra: document.getElementById('extra').value
	};
	
	let url = '/api/produtos';
	let method = 'POST';
	
	if(produtoEditandoId !== null) {
		url = `/api/produtos/${produtoEditandoId}`;
		method = 'PUT';
	}

	const res = await fetch(url, {
		method: method,
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(payload)
	});

	if(res.ok) {
		alert(produtoEditandoId ? 'Produto atualizado com sucesso!' : 'Produto cadastrado com sucesso!');
		cancelarEdicao(); // limpa o form e reseta o estado
		loadProdutos();
	} else {
		alert(`Erro ao ${produtoEditandoId ? 'atualizar' : 'cadastrar'} produto: ` + await res.text());
	}
});

// Solicita a criação de um pedido
async function simularPedido() {
	const res = await fetch('/api/pedidos/simular', { method: 'POST' });
	if(res.ok) {
		alert('Pedido simulado com sucesso!');
		loadPedidos();
	} else {
		alert('Erro ao simular pedido: ' + await res.text());
	}
}

// Inicialização
updateExtraLabel();
loadProdutos();
loadPedidos();
