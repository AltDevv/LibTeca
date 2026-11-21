import { esc,capa,vazio,icone } from '../mapper/VisualMapper.js';
export function catalogoTela(categorias,gestao=false,nome='LibTeca') {
    return '<div class="page-heading"><div>'+(!gestao?'<span class="library-label">'+esc(nome)+'</span>':'')+'<h1>'+(gestao?'Livros':'Catálogo')+'</h1></div>'+(gestao?'<button class="btn" data-acao="novo" data-recurso="livros">'+icone('mais')+'Cadastrar livro</button>':'')+'</div>'+
    '<section id="busca" class="catalog-section"><form id="filtro-livros" class="searchbar panel"><label class="title-search">Título<div class="search-field">'+icone('busca')+'<input name="titulo" placeholder="Buscar livro" aria-label="Buscar por título"></div></label><label>Autor<input name="autor" placeholder="Nome do autor" aria-label="Filtrar por autor"></label><label>Categoria<select name="categoria" aria-label="Categoria"><option value="">Todas</option>'+categorias.map(c=>'<option>'+esc(c.nome)+'</option>').join('')+'</select></label><label>Formato<select name="tipoLivro" aria-label="Formato"><option value="">Todos</option><option value="FISICO">Físico</option><option value="VIRTUAL">Digital</option></select></label><button class="btn" type="submit">Buscar</button></form>'+
    '<div class="filter-meta"><span class="muted" id="total-livros"></span><label class="check-label"><input type="checkbox" id="so-disponiveis"> Apenas disponíveis</label><button class="link-btn" data-acao="limpar-busca">Limpar filtros</button></div><div id="livros-grid" class="book-grid" aria-live="polite"></div><div id="paginacao" class="paginacao"></div></section>';
}
export function cardsLivros(pagina,administrador=false,gestao=administrador) {
    if(!pagina.content.length) return vazio('Nenhum livro encontrado','Experimente outro título ou remova os filtros.');
    return pagina.content.map(l=>'<article class="book-card"><a class="book-card-link" href="#/livro/'+l.id+'">'+capa(l)+
        '<div class="book-info"><span class="book-category">'+esc(l.categorias?.[0]?.nome||'Acervo')+'</span><h3>'+esc(l.titulo)+'</h3><p>'+esc(l.autores.map(a=>a.nome).join(', '))+'</p><span class="book-bottom">Ver exemplares '+icone('seta')+'</span></div></a>'+(gestao?'<button class="link-btn" data-acao="editar" data-recurso="livros" data-id="'+l.id+'">Editar livro</button>':'')+(administrador?'<button class="link-btn perigo excluir-livro" data-acao="excluir-livro" data-id="'+l.id+'" aria-label="Excluir livro '+esc(l.titulo)+'">Excluir livro</button>':'')+'</article>').join('');
}

export function cardsExemplo(numeros,administrador) {
    return numeros.map(numero=>'<article class="book-card exemplo">'+capa({id:numero,titulo:'Livro exemplo',autores:[]})+'<div class="book-info"><span class="book-category">Exemplo</span><h3>Livro exemplo</h3>'+(administrador?'<button class="link-btn" data-acao="remover-exemplo" data-id="'+numero+'" aria-label="Remover exemplo '+numero+'">Remover exemplo</button>':'')+'</div></article>').join('');
}
