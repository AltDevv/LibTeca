import { esc,capa,badge,data,vazio,botao,icone } from '../mapper/VisualMapper.js';
import { exemplaresCatalogo } from '../mapper/ExemplarMapper.js';
export function livroTela(livro,exemplares,sessao,emprestimos=[],reservas=[]) {
    exemplares=exemplaresCatalogo(exemplares);
    return '<a class="back-link" href="#/catalogo">← Voltar ao catálogo</a><div class="book-detail">'+capa(livro,'grande')+
        '<div class="book-description"><p class="eyebrow">'+esc(livro.categorias.map(c=>c.nome).join(' · '))+'</p><h1>'+esc(livro.titulo)+'</h1><p class="authors">por '+esc(livro.autores.map(a=>a.nome).join(', '))+'</p>'+
        '<dl class="book-facts"><div><dt>Editora</dt><dd>'+esc(livro.editora.nome)+'</dd></div><div><dt>Páginas</dt><dd>'+livro.numeroDePaginas+'</dd></div><div><dt>Lançamento</dt><dd>'+data(livro.dataLancamento)+'</dd></div></dl>'+
        (sessao.gestao?'<button class="btn secundario" data-acao="editar" data-recurso="livros" data-id="'+livro.id+'">Editar livro</button>':'')+(sessao.administrador?botao('Excluir livro','excluir-livro',livro.id,'btn secundario perigo'):'')+'<div class="notice">Solicite o livro físico e retire na biblioteca. Um funcionário ou administrador confirma a entrega e inicia o prazo escolhido por você. A leitura digital começa na hora.</div>'+
        (sessao.autenticado?'':'<p><a href="#/login">Entre na sua conta</a> para reservar ou começar uma leitura.</p>')+'</div></div>'+
        '<section class="panel"><div class="section-heading"><div><h2>Exemplares</h2></div></div>'+
        (exemplares.length?'<div class="exemplares-list">'+exemplares.map(e=>linhaExemplar(e,sessao,emprestimos,reservas,livro,exemplares)).join('')+'</div>':vazio('Ainda não há exemplares','A biblioteca ainda não disponibilizou exemplares deste título.'))+'</section>';
}

function linhaExemplar(e,sessao,emprestimos,reservas,livro,exemplares) {
    const fisico=e.tipo==='FISICO';
    const meu=sessao.autenticado&&fisico?emprestimos.find(p=>p.exemplar.id===e.id&&['PENDENTE','ATIVO','EXPIRADO'].includes(p.status)):null;
    const reserva=sessao.autenticado&&fisico?reservas.find(r=>r.exemplar.id===e.id&&r.status==='DISPONIVEL_PARA_RETIRADA'):null;
    let acoes='',aviso='',status=e.status;
    const leitura=!fisico&&emprestimos.find(p=>p.exemplar.id===e.id&&p.status==='ATIVO'&&!p.dataDevolucao&&p.dataExpiracao>=new Date().toLocaleDateString('en-CA'));
    if(leitura){
        acoes='<a class="btn" href="#/leitor/'+Number(leitura.id)+'">Continuar leitura</a>';
    }else if(meu?.status==='PENDENTE'){
        status='PENDENTE';aviso='Você já pode retirar este livro na biblioteca.';
        acoes='';
    }else if(meu){
        aviso='Recebimento confirmado. Devolução até '+data(meu.dataExpiracao)+'.';
    }else if(reserva){
        aviso='Seu livro está pronto para retirada na biblioteca.';
        acoes='<span class="muted">Aguardando entrega pelo funcionário</span>';
    }else if(sessao.autenticado&&e.status!=='INDISPONIVEL'){
        const lancado=livro.dataLancamento<=new Date().toLocaleDateString('en-CA');
        const disponivel=lancado&&exemplares.some(c=>c.tipo===e.tipo&&c.status==='DISPONIVEL');
        const limite=livro.limiteReservas??3;
        const jaReservou=reservas.some(r=>r.exemplar.livro.id===livro.id&&['ATIVA','DISPONIVEL_PARA_RETIRADA'].includes(r.status));
        if(e.status==='DISPONIVEL'&&lancado)acoes+=botao(fisico?'Emprestar':'Começar leitura',fisico?'emprestar-fisico':'emprestar-digital',e.id,'btn');
        else if(!disponivel&&limite>0&&!jaReservou){
            if((livro.reservasEmAndamento??0)<limite)acoes+=botao('Reservar','reservar',e.id,'btn secundario');
            else aviso='Limite de reservas atingido.';
        }
    }
    return '<div class="exemplar-row"><div class="exemplar-icon">'+icone(fisico?'livro':'exemplares')+'</div><div><strong>'+(fisico?'Exemplar físico':'Edição digital')+'</strong><small>Exemplar #'+e.id+'</small>'+(aviso?'<small>'+esc(aviso)+'</small>':'')+'</div>'+badge(status)+'<div class="row-actions">'+acoes+'</div></div>';
}
