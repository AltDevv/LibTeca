import { esc,capa,badge,data,vazio,botao,icone } from '../mapper/VisualMapper.js';
export function livroTela(livro,exemplares,sessao) {
    return '<a class="back-link" href="#/catalogo">← Voltar ao catálogo</a><div class="book-detail">'+capa(livro,'grande')+
        '<div class="book-description"><p class="eyebrow">'+esc(livro.categorias.map(c=>c.nome).join(' · '))+'</p><h1>'+esc(livro.titulo)+'</h1><p class="authors">por '+esc(livro.autores.map(a=>a.nome).join(', '))+'</p>'+
        '<dl class="book-facts"><div><dt>Editora</dt><dd>'+esc(livro.editora.nome)+'</dd></div><div><dt>Páginas</dt><dd>'+livro.numeroDePaginas+'</dd></div><div><dt>Lançamento</dt><dd>'+data(livro.dataLancamento)+'</dd></div></dl>'+
        (sessao.gestao?'<button class="btn secundario" data-acao="editar" data-recurso="livros" data-id="'+livro.id+'">Editar livro</button>':'')+(sessao.administrador?botao('Excluir livro','excluir-livro',livro.id,'btn secundario perigo'):'')+'<div class="notice">Reserve um exemplar físico para retirar na biblioteca ou solicite um empréstimo digital por 14 dias.</div>'+
        (sessao.autenticado?'':'<p><a href="#/login">Entre na sua conta</a> para reservar ou começar uma leitura.</p>')+'</div></div>'+
        '<section class="panel"><div class="section-heading"><div><h2>Exemplares</h2></div><span class="badge">'+exemplares.length+' no acervo</span></div>'+
        (exemplares.length?'<div class="exemplares-list">'+exemplares.map(e=>'<div class="exemplar-row"><div class="exemplar-icon">'+icone(e.tipo==='VIRTUAL'?'exemplares':'livro')+'</div><div><strong>'+ (e.tipo==='VIRTUAL'?'Edição digital':'Exemplar físico')+'</strong><small>Exemplar #'+e.id+'</small></div>'+badge(e.status)+'<div class="row-actions">'+
            (sessao.autenticado&&e.status!=='INDISPONIVEL'&&e.status!=='RESERVADO'?(e.tipo==='VIRTUAL'&&e.status==='DISPONIVEL'?botao('Começar leitura','emprestar-digital',e.id,'btn'):botao('Reservar','reservar',e.id,'btn secundario')):'')+'</div></div>').join('')+'</div>':vazio('Ainda não há exemplares','A biblioteca ainda não disponibilizou exemplares deste título.'))+'</section>';
}
