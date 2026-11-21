import {nomesPrazos} from './PrazoTela.js';
import {esc,badge,data,vazio,botao,icone} from '../mapper/VisualMapper.js';
export function circulacaoTela(itens,tipo,gestao=false){
    const reserva=tipo==='reservas';
    return '<div class="page-heading"><div><h1>'+(gestao?'':reserva?'Minhas ':'Meus ')+(reserva?'Reservas':'Empréstimos')+'</h1></div>'+
    (gestao?'<button class="btn" data-acao="novo" data-recurso="'+tipo+'">＋ '+(reserva?'Nova reserva':'Novo empréstimo')+'</button>':'<a href="#/catalogo" class="btn secundario">Explorar catálogo ↗</a>')+'</div>'+
    '<div class="panel"><div class="section-heading"><h2>'+itens.length+' registros</h2></div>'+
    (itens.length?'<div class="loan-list">'+itens.map(e=>{
        const aberto=reserva?['ATIVA','DISPONIVEL_PARA_RETIRADA'].includes(e.status):!e.dataDevolucao&&['PENDENTE','ATIVO','EXPIRADO'].includes(e.status);
        const digital=e.exemplar.tipo==='VIRTUAL';
        const pendente=!reserva&&e.status==='PENDENTE';
        const podeLer=digital&&e.status==='ATIVO'&&!e.dataDevolucao&&e.dataExpiracao>=new Date().toLocaleDateString('en-CA');
        let acoes='';
        if(reserva&&aberto) {acoes+=botao('Cancelar',gestao?'cancelar-gestao':'cancelar',e.id,'link-btn perigo');
            if(e.status==='DISPONIVEL_PARA_RETIRADA') {
                if(gestao||digital) acoes+=botao(gestao?(digital?'Retirar':'Confirmar entrega'):'Iniciar leitura',gestao?'retirar-gestao':'retirar',e.id,'btn pequeno');
                else acoes+='<span class="muted">Aguardando entrega pelo funcionário</span>';
            }}
        if(pendente){
            if(gestao)acoes+=botao('Confirmar entrega','entregar-gestao',e.id,'btn pequeno');
            acoes+=botao('Cancelar solicitação',gestao?'cancelar-emprestimo-gestao':'cancelar-emprestimo',e.id,'link-btn perigo');
        }
        if(!reserva&&aberto&&!pendente) {
            if(podeLer&&!gestao) acoes+='<a class="btn pequeno" href="#/leitor/'+e.id+'">Ler agora ↗</a>';
            if(gestao||digital) acoes+=botao('Devolver',gestao?'devolver-gestao':'devolver',e.id,'btn secundario pequeno');
            else acoes+='<span class="muted">Devolva na biblioteca</span>';
        }
        const detalhe=reserva?'Reservado em '+data(e.dataReserva):pendente?'Solicitado em '+data(e.dataEmprestimo)+' · '+(gestao?'Aguardando retirada na biblioteca':'Você já pode retirar o livro na biblioteca'):e.status==='CANCELADO'?'Solicitação cancelada antes da retirada':'Prazo: '+data(e.dataExpiracao)+(e.dataDevolucao?' · Devolvido em '+data(e.dataDevolucao):'');
        return '<article class="loan-row"><div class="loan-icon">'+icone(digital?'exemplares':'livro')+'</div><div class="loan-title"><strong>'+esc(e.exemplar.livro.titulo)+'</strong><small>'+ (digital?'Digital':'Físico')+' · Exemplar #'+e.exemplar.id+(gestao?' · '+esc(e.usuario.nome):'')+'</small><span class="muted">'+detalhe+(!reserva&&e.prazo?' · Duração: '+nomesPrazos[e.prazo]:'')+'</span></div>'+badge(e.status)+'<div class="row-actions">'+acoes+'</div></article>';
    }).join('')+'</div>':vazio(reserva?'Nenhuma reserva':'Nenhum empréstimo','',gestao?'':'<a class="btn secundario" href="#/catalogo">Ver catálogo</a>'))+'</div>';
}
