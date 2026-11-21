import {esc,badge,data,vazio,icone} from '../mapper/VisualMapper.js';
const titulos={exemplares:'Exemplares',usuarios:'Usuários',funcionarios:'Equipe',autores:'Autores',categorias:'Categorias',editoras:'Editoras'};
const singular={exemplares:'exemplar',usuarios:'usuário',funcionarios:'funcionário',autores:'autor',categorias:'categoria',editoras:'editora'};
const cadastrar=recurso=>'<button class="btn" data-acao="novo" data-recurso="'+recurso+'">'+icone('mais')+'Cadastrar '+singular[recurso]+'</button>';
export function painelTela(dados){
    const abertos=dados.emprestimos.filter(e=>!e.dataDevolucao&&['PENDENTE','ATIVO','EXPIRADO'].includes(e.status));
    const ativos=abertos.filter(e=>e.status!=='PENDENTE');
    const atrasados=ativos.filter(e=>e.dataExpiracao<new Date().toLocaleDateString('en-CA'));
    return '<div class="page-heading"><h1>Visão geral</h1><button class="btn" data-acao="novo" data-recurso="emprestimos">'+icone('mais')+'Novo empréstimo</button></div><div class="stats">'+
    [['Livros',dados.livros.length,'livro','acervo'],['Disponíveis',dados.exemplares.filter(e=>e.status==='DISPONIVEL').length,'exemplares','exemplares'],['Empréstimos abertos',abertos.length,'emprestimo','circulacao'],['Em atraso',atrasados.length,'reserva','circulacao']].map(([n,v,i,rota])=>'<a href="#/'+rota+'" class="stat"><span>'+n+icone(i)+'</span><strong>'+v+'</strong></a>').join('')+
    '</div><div class="dashboard-columns"><section class="panel"><div class="section-heading"><h2>Próximas devoluções</h2><a class="link-btn" href="#/circulacao">Ver empréstimos '+icone('seta')+'</a></div>'+
    (ativos.length?[...ativos].sort((a,b)=>a.dataExpiracao.localeCompare(b.dataExpiracao)).slice(0,6).map(e=>'<div class="compact-row"><div><strong>'+esc(e.exemplar.livro.titulo)+'</strong><small>'+esc(e.usuario.nome)+'</small></div><span class="'+(e.dataExpiracao<new Date().toLocaleDateString('en-CA')?'perigo':'')+'">'+data(e.dataExpiracao)+'</span></div>').join(''):vazio('Nenhum empréstimo em aberto',''))+
    '</section><section class="panel"><div class="section-heading"><h2>Acesso rápido</h2></div><div class="quick-links">'+[['acervo','Livros','livro'],['exemplares','Exemplares','exemplares'],['cadastros','Autores, categorias e editoras','cadastros'],['usuarios','Usuários','usuarios']].map(([id,n,i])=>'<a href="#/'+id+'">'+icone(i)+'<span>'+n+'</span>'+icone('seta')+'</a>').join('')+'</div></section></div>';
}
export function tabelaTela(recurso,itens,auxiliar=false,formato='fisicos',sessao={}){
    const equipeLeitura=recurso==='funcionarios'&&!sessao.administrador;
    const botaoCadastro=equipeLeitura?'':cadastrar(recurso);
    const admin=e=>['usuarios','funcionarios'].includes(recurso)&&['ADMIN','ADMIM'].includes((recurso==='usuarios'?e:e.usuario)?.role);
    const proprio=e=>sessao.administrador&&sessao.usuario?.id===(recurso==='usuarios'?e:e.usuario)?.id;
    const virtual=recurso==='exemplares'&&formato==='virtuais';
    const ordem=new Map(itens.filter(e=>e.tipo==='VIRTUAL').sort((a,b)=>a.id-b.id).map((e,i)=>[e.id,i+1]));
    if(recurso==='exemplares')itens=itens.filter(e=>virtual?e.tipo==='VIRTUAL':e.tipo==='FISICO');
    const celulas=e=>recurso==='exemplares'?(virtual?[esc(e.livro.titulo),badge(e.status),Number(e.emprestimosAtivos||0),Number(e.totalEmprestimos||0)]:[esc(e.livro.titulo),badge(e.tipo),badge(e.status),esc(e.codigoPatrimonio||'—')]):
        recurso==='usuarios'?[esc(e.nome),esc(e.email),badge(e.role)]:
        recurso==='funcionarios'?[esc(e.usuario.nome),esc(e.usuario.email),data(e.dataAdmissao),badge(e.ativo?'ATIVO':'INDISPONIVEL')]:[esc(e.nome)];
    const colunas=recurso==='exemplares'?(virtual?['Livro','Status','Leituras ativas','Total de empréstimos']:['Livro','Formato','Status','Patrimônio']):recurso==='usuarios'?['Nome','Email','Perfil']:recurso==='funcionarios'?['Nome','Email','Admissão','Situação']:['Nome'];
    return (auxiliar?'':'<div class="page-heading"><h1>'+titulos[recurso]+'</h1>'+botaoCadastro+'</div>')+
    (recurso==='exemplares'?'<nav class="tabs" aria-label="Formato dos exemplares"><a href="#/exemplares/fisicos" '+(!virtual?'class="active" aria-current="page"':'')+'>Físicos</a><a href="#/exemplares/virtuais" '+(virtual?'class="active" aria-current="page"':'')+'>Virtuais</a></nav>':'')+'<section class="panel table-panel"><div class="list-toolbar"><div><h2>'+titulos[recurso]+' <span class="count">'+itens.length+'</span></h2></div><label class="search-field">'+icone('busca')+'<input id="busca-tabela" placeholder="Buscar por nome'+(recurso==='usuarios'?' ou email':'')+'" aria-label="Filtrar lista" '+(!itens.length?'disabled':'')+'></label>'+(auxiliar?cadastrar(recurso):'')+'</div>'+
    (itens.length?'<div class="table-scroll"><table><thead><tr><th scope="col">ID</th>'+colunas.map(c=>'<th scope="col">'+c+'</th>').join('')+'<th scope="col" class="action-heading">Ações</th></tr></thead><tbody>'+itens.map(e=>'<tr><td class="muted">'+(virtual?'V'+String(ordem.get(e.id)).padStart(3,'0'):'#'+e.id)+'</td>'+celulas(e).map((c,i)=>'<td class="'+(i===0?'cell-title':'')+'">'+c+'</td>').join('')+'<td><div class="row-actions">'+
    (recurso==='exemplares'?'<button class="link-btn" data-acao="editar" data-recurso="exemplares" data-id="'+e.id+'">Editar</button>':'')+
    (recurso==='funcionarios'&&!equipeLeitura&&!admin(e)?'<button class="link-btn" data-acao="alternar-funcionario" data-id="'+e.id+'">'+(e.ativo?'Desativar':'Ativar')+'</button>':'')+
    (admin(e)?(proprio(e)?'<a class="link-btn" href="#/configuracao">Gerenciar administração</a>':'<span class="muted">Administrador</span>'):
    ((equipeLeitura||(recurso==='usuarios'&&e.role==='FUNCIONARIO'&&!sessao.administrador))?'<span class="muted">—</span>':'<button class="link-btn perigo" data-acao="excluir" data-recurso="'+recurso+'" data-id="'+e.id+'" aria-label="Excluir '+esc(e.nome||e.livro?.titulo||e.usuario?.nome||'cadastro')+'">Excluir</button>'))+'</div></td></tr>').join('')+'</tbody></table></div><p id="sem-filtro" hidden class="empty-filter">Nenhum registro encontrado.</p>':vazio('Nenhum registro cadastrado','Os novos cadastros aparecerão nesta lista.'))+'</section>';
}
export function cadastrosTela(recurso,itens){
    return '<div class="page-heading"><h1>Cadastros</h1></div><nav class="tabs" aria-label="Tipos de cadastro">'+['autores','categorias','editoras'].map(id=>'<a href="#/cadastros/'+id+'" '+(id===recurso?'aria-current="page" class="active"':'')+'>'+icone(id)+titulos[id]+'</a>').join('')+'</nav>'+tabelaTela(recurso,itens,true);
}
