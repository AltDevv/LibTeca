import {esc,badge,data,vazio,botao,icone} from '../mapper/VisualMapper.js';
export function circulacaoTela(itens,tipo,gestao=false){
    const reserva=tipo==='reservas';
    return '<div class="page-heading"><div><h1>'+(gestao?'':reserva?'Minhas ':'Meus ')+(reserva?'Reservas':'Empréstimos')+'</h1></div>'+
    (gestao?'<button class="btn" data-acao="novo" data-recurso="'+tipo+'">＋ '+(reserva?'Nova reserva':'Novo empréstimo')+'</button>':'<a href="#/catalogo" class="btn secundario">Explorar catálogo ↗</a>')+'</div>'+
    '<div class="panel"><div class="section-heading"><h2>'+itens.length+' registros</h2></div>'+
    (itens.length?'<div class="loan-list">'+itens.map(e=>{
        const aberto=reserva?['ATIVA','DISPONIVEL_PARA_RETIRADA'].includes(e.status):!e.dataDevolucao;
        const digital=e.exemplar.tipo==='VIRTUAL';
        const podeLer=digital&&e.status==='ATIVO'&&!e.dataDevolucao&&e.dataExpiracao>=new Date().toLocaleDateString('en-CA');
        let acoes='';
        if(reserva&&aberto) {acoes+=botao('Cancelar',gestao?'cancelar-gestao':'cancelar',e.id,'link-btn perigo');
            if(e.status==='DISPONIVEL_PARA_RETIRADA'&&(gestao||digital)) acoes+=botao('Retirar',gestao?'retirar-gestao':'retirar',e.id,'btn pequeno');}
        if(!reserva&&aberto) {
            if(podeLer&&!gestao) acoes+='<a class="btn pequeno" href="#/leitor/'+e.id+'">Ler agora ↗</a>';
            if(gestao||digital) acoes+=botao('Devolver',gestao?'devolver-gestao':'devolver',e.id,'btn secundario pequeno');
            else acoes+='<span class="muted">Devolva na biblioteca</span>';
        }
        return '<article class="loan-row"><div class="loan-icon">'+icone(digital?'exemplares':'livro')+'</div><div class="loan-title"><strong>'+esc(e.exemplar.livro.titulo)+'</strong><small>'+ (digital?'Digital':'Físico')+' · Exemplar #'+e.exemplar.id+(gestao?' · '+esc(e.usuario.nome):'')+'</small><span class="muted">'+(reserva?'Reservado em '+data(e.dataReserva):'Prazo: '+data(e.dataExpiracao)+(e.dataDevolucao?' · Devolvido em '+data(e.dataDevolucao):''))+'</span></div>'+badge(e.status)+'<div class="row-actions">'+acoes+'</div></article>';
    }).join('')+'</div>':vazio(reserva?'Nenhuma reserva':'Nenhum empréstimo','',gestao?'':'<a class="btn secundario" href="#/catalogo">Ver catálogo</a>'))+'</div>';
}
