import { prepararCapa,arquivoCapa } from './CapaController.js';
import { configuracaoTela } from '../telas/ConfiguracaoTela.js';
import { exemplosVisiveis,removerExemplo } from '../entity/LivrosExemplo.js';
import { Sessao } from '../entity/Sessao.js';
import { biblioteca,autenticacao,configuracao } from '../service/BibliotecaService.js';
import { dadosFormulario } from '../dto/FormularioRequest.js';
import { filtroLivro } from '../filter/LivroFiltro.js';
import { podeAbrir } from '../specification/Permissoes.js';
import { avisar,mensagemErro } from '../handler/ErroHandler.js';
import { esc,vazio,botao } from '../mapper/VisualMapper.js';
import { estrutura } from '../telas/EstruturaTela.js';
import { catalogoTela,cardsLivros,cardsExemplo } from '../telas/CatalogoTela.js';
import { livroTela } from '../telas/LivroTela.js';
import { autenticacaoTela } from '../telas/AutenticacaoTela.js';
import { circulacaoTela } from '../telas/CirculacaoTela.js';
import { painelTela,tabelaTela,cadastrosTela } from '../telas/GestaoTela.js';
import { perfilTela } from '../telas/PerfilTela.js';
import { formularioTela } from '../telas/FormularioTela.js';

let info={nome:'LibTeca',configurada:false},sessao=new Sessao(), registros=[], pagina=0, versao=0, buscaVersao=0;
const modal=document.querySelector('#modal');
const titulos={configuracao:'Minha biblioteca',catalogo:'Catálogo',livro:'Detalhes do livro',login:'Entrar',cadastro:'Criar conta',emprestimos:'Meus empréstimos',reservas:'Minhas reservas',painel:'Visão geral',acervo:'Livros',exemplares:'Exemplares',usuarios:'Usuários',cadastros:'Cadastros',equipe:'Equipe',circulacao:'Empréstimos',fila:'Reservas',perfil:'Minha conta',leitor:'Leitor digital'};
const rotaAtual=()=>location.hash.replace(/^#\/?/,'').split('/').filter(Boolean);
const conteudo=()=>document.querySelector('#conteudo');
const atualizarFormato=()=>{const tipo=modal.querySelector('#tipo-exemplar')?.value;if(!tipo)return;modal.querySelectorAll('[data-formato]').forEach(grupo=>{const oculto=grupo.dataset.formato!==tipo;grupo.hidden=oculto;grupo.querySelectorAll('input').forEach(campo=>campo.disabled=oculto);});};
const abrirModal=html=>{modal.innerHTML=html;prepararCapa(modal.querySelector('form'));atualizarFormato();modal.showModal();modal.querySelector('input:not([disabled]),select:not([disabled])')?.focus();};
const fecharModal=()=>modal.close();
const fecharMenu=()=>{document.body.classList.remove('menu-aberto');const botao=document.querySelector('.menu-toggle');botao?.setAttribute('aria-expanded','false');botao?.focus();};
document.addEventListener('keydown',e=>{if(e.key==='Escape'&&document.body.classList.contains('menu-aberto'))fecharMenu();});
const carregando=()=>'<div class="loading" role="status">Buscando na biblioteca…</div>';

async function navegar(){
    const minhaVersao=++versao;
    let [rota='catalogo',id]=rotaAtual();
    document.body.classList.remove('menu-aberto');
    if(modal.open) modal.close();
    try { info=await configuracao.informacoes(); }
    catch(erro){document.querySelector('#app').innerHTML=estrutura(sessao,rota,info);conteudo().innerHTML=vazio('Não foi possível conectar',mensagemErro(erro),botao('Tentar novamente','recarregar','','btn'));return;}
    if(minhaVersao!==versao)return;
    if(!info.configurada && rota!=='configuracao' && !(rota==='login'&&info.possuiAdministrador)){location.hash='/configuracao';return;}
    if(rota==='configuracao' && info.configurada && !sessao.administrador){location.hash='/catalogo';return;}
    if(!podeAbrir(rota,sessao)){
        if(!sessao.autenticado){location.hash='/login';return;}
        avisar('Seu perfil não tem acesso a esta área.',true);location.hash='/catalogo';return;
    }
    document.querySelector('#app').innerHTML=estrutura(sessao,rota,info);
    document.querySelector('#local-atual').textContent=titulos[rota]||'Biblioteca';
    document.title=info.nome;
    conteudo().innerHTML=carregando();
    try {
        let html='';
        if(['catalogo','acervo'].includes(rota)){
            const categorias=await biblioteca.listar('categorias');
            if(minhaVersao!==versao)return;
            conteudo().innerHTML=catalogoTela(categorias,rota==='acervo',info.nome);pagina=0;await buscarCatalogo();return;
        }
        if(rota==='configuracao') html=configuracaoTela(info,sessao);
        else if(rota==='livro'){const [livro,exemplares]=await Promise.all([biblioteca.livro(id),biblioteca.exemplares(id)]);html=livroTela(livro,exemplares,sessao);}
        else if(rota==='login'||rota==='cadastro') html=autenticacaoTela(rota==='cadastro');
        else if(['emprestimos','reservas','circulacao','fila'].includes(rota)){
            const gestao=['circulacao','fila'].includes(rota),tipo=['reservas','fila'].includes(rota)?'reservas':'emprestimos';
            registros=gestao?await biblioteca.listar(tipo):await biblioteca.meus(tipo);
            html=circulacaoTela(registros,tipo,gestao);
        } else if(rota==='painel'){
            const [livros,exemplares,emprestimos]=await Promise.all(['livros','exemplares','emprestimos'].map(biblioteca.listar));
            html=painelTela({livros,exemplares,emprestimos});
        } else if(['exemplares','usuarios','equipe'].includes(rota)||rota==='cadastros'){
            const recurso=rota==='equipe'?'funcionarios':rota==='cadastros'?(id||'autores'):rota;
            if(!['exemplares','usuarios','funcionarios','autores','categorias','editoras'].includes(recurso))throw new Error('Página não encontrada.');
            registros=await biblioteca.listar(recurso);html=rota==='cadastros'?cadastrosTela(recurso,registros):tabelaTela(recurso,registros);
        }
        else if(rota==='perfil') html=perfilTela(sessao,await biblioteca.meus('telefones'));
        else if(rota==='leitor'){
            const meus=await biblioteca.meus('emprestimos');
            const emprestimo=meus.find(e=>String(e.id)===id);
            if(!emprestimo||emprestimo.exemplar.tipo!=='VIRTUAL'||emprestimo.dataDevolucao||emprestimo.status!=='ATIVO'||emprestimo.dataExpiracao<new Date().toLocaleDateString('en-CA'))throw new Error('Não há um empréstimo digital vigente para esta leitura.');
            html='<a class="back-link" href="#/emprestimos">← Meus empréstimos</a><div class="page-heading"><div><p class="eyebrow">SUA LEITURA</p><h1>'+esc(emprestimo.exemplar.livro.titulo)+'</h1></div><a class="btn secundario" href="/api/arquivos/leitura/'+Number(id)+'" target="_blank" rel="noopener">Abrir leitura em outra aba ↗</a></div><p class="muted">Se seu navegador não mostrar o PDF abaixo, use o botão de abrir em outra aba.</p><iframe class="leitor" title="Leitura do livro" src="/api/arquivos/leitura/'+Number(id)+'#toolbar=0"></iframe>';
        } else html=vazio('Página não encontrada','Volte ao catálogo para continuar.','<a class="btn" href="#/catalogo">Ir ao catálogo</a>');
        if(minhaVersao!==versao)return;
        conteudo().innerHTML=html;
    }catch(erro){
        if(minhaVersao!==versao)return;
        conteudo().innerHTML=vazio('Não conseguimos carregar esta página',mensagemErro(erro),botao('Tentar novamente','recarregar','','btn'));
    }
}
async function buscarCatalogo(){
    const form=document.querySelector('#filtro-livros');if(!form)return;
    const requisicao=++buscaVersao,versaoTela=versao;
    const params=filtroLivro(form,pagina);
    if(document.querySelector('#so-disponiveis').checked)params.set('quantidadeMinima','1');
    document.querySelector('#livros-grid').innerHTML=carregando();
    try {
        const resultado=await biblioteca.buscarLivros(params);
        if(requisicao!==buscaVersao||versaoTela!==versao)return;
        const semFiltro=![...new FormData(form).values()].some(Boolean)&&!document.querySelector('#so-disponiveis').checked;
        const exemplos=resultado.totalElements===0&&semFiltro?exemplosVisiveis(info,localStorage):[];
        document.querySelector('#livros-grid').innerHTML=exemplos.length?cardsExemplo(exemplos,sessao.administrador):cardsLivros(resultado,sessao.administrador,sessao.gestao);
        document.querySelector('#total-livros').textContent=exemplos.length?'Prévia do acervo':resultado.totalElements+' '+(resultado.totalElements===1?'livro':'livros');
        document.querySelector('#paginacao').innerHTML=resultado.totalPages>1?
            '<button class="btn secundario pequeno" data-acao="pagina-anterior" '+(pagina===0?'disabled':'')+'>← Anterior</button><span>Página '+(pagina+1)+' de '+resultado.totalPages+'</span><button class="btn secundario pequeno" data-acao="pagina-proxima" '+(resultado.last?'disabled':'')+'>Próxima →</button>':'';
    }catch(erro){if(versaoTela===versao)document.querySelector('#livros-grid').innerHTML=vazio('Não foi possível buscar',mensagemErro(erro),botao('Tentar novamente','buscar','','btn'));}
}
async function novo(recurso){
    const dependencias={livros:['autores','categorias','editoras'],exemplares:['livros'],funcionarios:['usuarios'],emprestimos:['usuarios','exemplares'],reservas:['usuarios','exemplares']}[recurso]||[];
    const opcoes={};await Promise.all(dependencias.map(async nome=>{opcoes[nome]=await biblioteca.listar(nome);}));
    abrirModal(formularioTela(recurso,opcoes));
}
async function editar(recurso,id){
    const registro=await biblioteca.obter(recurso,id);
    await novo(recurso);
    const form=modal.querySelector('#form-cadastro');form.dataset.id=id;
    modal.querySelector('#modal-titulo').textContent='Editar '+(recurso==='livros'?'livro':'exemplar');
    form.querySelector('button[type="submit"]').textContent='Salvar alterações';
    const valores=recurso==='livros'?{...registro,editoraId:registro.editora.id,autoresIds:registro.autores.map(a=>a.id),categoriasIds:registro.categorias.map(c=>c.id)}:{...registro,livroId:registro.livro.id};
    for(const campo of form.elements){if(!campo.name||campo.type==='file')continue;
        if(campo.dataset.listaNumero!==undefined)campo.checked=valores[campo.name]?.includes(Number(campo.value));
        else campo.value=valores[campo.name]??'';
    }
    atualizarFormato();
}
async function confirmar(titulo,texto,operacao,destino){
    abrirModal('<div class="modal-header"><h2 id="modal-titulo">'+esc(titulo)+'</h2><button class="icon-btn" data-acao="fechar-modal" aria-label="Fechar">×</button></div><p style="margin-top:20px">'+esc(texto)+'</p><p class="form-error" role="alert"></p><div class="modal-footer"><button class="btn secundario" data-acao="fechar-modal">Voltar</button><button class="btn" id="confirmar-acao">Confirmar</button></div>');
    document.querySelector('#confirmar-acao').onclick=async e=>{
        e.target.disabled=true;e.target.textContent='Aguarde…';
        try{await operacao();fecharModal();avisar('Operação concluída.');if(destino)location.hash=destino;else await navegar();}
        catch(erro){modal.querySelector('.form-error').textContent=mensagemErro(erro);e.target.disabled=false;e.target.textContent='Confirmar';}
    };
}
document.addEventListener('click',async evento=>{
    if(evento.target.closest('.skip')){evento.preventDefault();conteudo().focus();return;}
    if(evento.target.id==='sombra-menu'){fecharMenu();return;}
    const alvo=evento.target.closest('[data-acao]');if(!alvo)return;
    const acao=alvo.dataset.acao,id=alvo.dataset.id;
    try{
        if(acao==='remover-exemplo'){removerExemplo(Number(id),localStorage);await buscarCatalogo();return;}
        if(acao==='fechar-menu'){fecharMenu();return;}
        if(acao==='menu'){const aberto=document.body.classList.toggle('menu-aberto');alvo.setAttribute('aria-expanded',aberto);if(aberto)document.querySelector('#sidebar .nav-item').focus();return;}
        if(acao==='fechar-modal'){fecharModal();return;}
        if(acao==='recarregar'){await navegar();return;}
        if(acao==='sair'){await autenticacao.sair();sessao=new Sessao();location.hash='/catalogo';await navegar();return;}
        if(acao==='editar'){alvo.disabled=true;await editar(alvo.dataset.recurso,id);return;}
        if(acao==='novo'){alvo.disabled=true;await novo(alvo.dataset.recurso);return;}
        if(acao==='limpar-busca'){document.querySelector('#filtro-livros').reset();document.querySelector('#so-disponiveis').checked=false;pagina=0;await buscarCatalogo();return;}
        if(acao.startsWith('pagina-')){pagina+=acao==='pagina-proxima'?1:-1;await buscarCatalogo();return;}
        if(acao==='buscar'){await buscarCatalogo();return;}
        if(acao==='excluir-livro'){
            if(!sessao.administrador)throw new Error('Apenas administradores podem excluir livros por esta tela.');
            const livro=await biblioteca.livro(id);
            await confirmar('Excluir livro?', 'Excluir “'+livro.titulo+'”? Esta ação não pode ser desfeita. Livros com exemplares vinculados não podem ser excluídos.',()=>biblioteca.excluir('livros',id),rotaAtual()[0]==='livro'?'/acervo':null);return;
        }
        if(acao==='excluir'){const registro=registros.find(r=>String(r.id)===id);const nome=registro?.nome||registro?.usuario?.nome||registro?.livro?.titulo||'este registro';await confirmar('Excluir cadastro?', 'Excluir “'+nome+'”? Registros com vínculos não podem ser excluídos.',()=>biblioteca.excluir(alvo.dataset.recurso,id));return;}
        if(acao==='alternar-funcionario'){
            const f=registros.find(e=>String(e.id)===id);
            await confirmar(f.ativo?'Desativar funcionário?':'Ativar funcionário?','A situação determina se esta conta pode trabalhar na biblioteca.',
                ()=>biblioteca.alterar('/funcionarios/'+id,{usuarioId:f.usuario.id,dataAdmissao:f.dataAdmissao,ativo:!f.ativo},'PUT'));return;
        }
        if(acao==='retirar-gestao'){
            abrirModal('<div class="modal-header"><h2 id="modal-titulo">Retirar reserva</h2><button class="icon-btn" data-acao="fechar-modal" aria-label="Fechar">×</button></div><form id="form-retirada" data-id="'+Number(id)+'"><label>Prazo de devolução<input type="date" name="dataPrevistaDevolucao" min="'+new Date().toLocaleDateString('en-CA')+'" required></label><p class="form-error" role="alert"></p><button class="btn">Confirmar retirada</button></form>');return;
        }
        const operacoes={
            'reservar':['Reservar exemplar?','Você poderá acompanhar a situação em Minhas reservas.','/minha-conta/reservas/'+id,'POST'],
            'emprestar-digital':['Começar esta leitura?','O empréstimo digital terá prazo de 14 dias.','/minha-conta/emprestimos/'+id,'POST'],
            'cancelar':['Cancelar reserva?','O exemplar deixará de ficar reservado para você.','/minha-conta/reservas/'+id+'/cancelar','PATCH'],
            'retirar':['Iniciar empréstimo digital?','O prazo de leitura será de 14 dias.','/minha-conta/reservas/'+id+'/retirar','PATCH'],
            'devolver':['Devolver exemplar?','O empréstimo será encerrado e o acesso digital terminará.','/minha-conta/emprestimos/'+id+'/devolver','PATCH'],
            'cancelar-gestao':['Cancelar reserva?','A reserva será encerrada.','/reservas/'+id+'/cancelar','PATCH'],
            'devolver-gestao':['Registrar devolução?','Confirme que o exemplar foi devolvido.','/emprestimos/'+id+'/devolver','PATCH'],
            'remover-telefone':['Remover telefone?','Este número será retirado da sua conta.','/minha-conta/telefones/'+id,'DELETE']
        };
        if(operacoes[acao]){
            const [titulo,texto,caminho,method]=operacoes[acao];
            await confirmar(titulo,texto,()=>biblioteca.alterar(caminho,undefined,method));
        }
    }catch(erro){avisar(mensagemErro(erro),true);}
    finally{alvo.disabled=false;}
});
document.addEventListener('submit',async evento=>{
    const form=evento.target;evento.preventDefault();
    if(form.id==='filtro-livros'){pagina=0;await buscarCatalogo();return;}
    const enviar=form.querySelector('button[type="submit"],button:not([type])');
    const textoEnviar=enviar?.innerHTML;
    if(enviar){enviar.disabled=true;enviar.textContent='Aguarde…';}
    const erroCampo=form.querySelector('.form-error');if(erroCampo)erroCampo.textContent='';
    try {
        const dados=dadosFormulario(form);
        if(form.id==='form-banco'){
            await configuracao.escolherBanco(dados);
            document.querySelector('#banco-progresso').textContent='Banco verificado. Iniciando o LibTeca…';
            let pronta=false;
            for(let tentativa=0;tentativa<60;tentativa++){
                await new Promise(resolve=>setTimeout(resolve,1000));
                let estado;try{estado=await configuracao.informacoes();}catch{continue;}
                if(estado.erroBanco)throw new Error(estado.erroBanco);
                if(!estado.escolherBanco){pronta=true;break;}
            }
            if(!pronta)throw new Error('O servidor ainda está iniciando. Aguarde e atualize a página para continuar.');
            await autenticacao.renovar();await navegar();
            const codigo=document.querySelector('[name="codigo"]');if(codigo)codigo.value=dados.codigo;
        } else if(form.id==='form-configuracao'){
            const identidade={nome:dados.nome,site:dados.site};
            if(form.dataset.instalar==='true'){
                if(dados.senha!==dados.confirmacao)throw new Error('As senhas precisam ser iguais.');
                info=await configuracao.instalar({biblioteca:identidade,codigo:dados.codigo,nomeAdministrador:dados.nomeAdministrador,email:dados.email,senha:dados.senha});
                // A instalação já foi concluída mesmo se houver falha de rede no login seguinte.
                try { sessao=new Sessao(await autenticacao.entrar({email:dados.email,senha:dados.senha})); }
                catch { avisar('Biblioteca criada. Entre com seu email e senha.');location.hash='/login';await navegar();return; }
            }else info=await configuracao.salvar(identidade);
            avisar('Sua biblioteca está pronta.');location.hash='/catalogo';await navegar();
        } else if(form.id==='form-auth'){
            if(form.dataset.cadastro==='true'){await autenticacao.cadastrar(dados);sessao=new Sessao(await autenticacao.entrar({email:dados.email,senha:dados.senha}));avisar('Sua conta foi criada. Boas leituras!');}
            else sessao=new Sessao(await autenticacao.entrar(dados));
            location.hash='/catalogo';await navegar();
        } else if(form.id==='form-cadastro'){
            const recurso=form.dataset.recurso;
            for(const grupo of form.querySelectorAll('[data-selecao]')){if(!dados[grupo.dataset.selecao]?.length)throw new Error('Selecione pelo menos uma opção em '+grupo.querySelector('legend').textContent+'.');}
            const capa=await arquivoCapa(form),pdf=form.elements.uploadLivro?.disabled?undefined:form.elements.uploadLivro?.files[0];
            if(capa)dados.capa=(await biblioteca.enviar(capa,'capa')).caminho;
            if(pdf)dados.caminhoArquivo=(await biblioteca.enviar(pdf,'livro')).caminho;
            if(form.dataset.id)await biblioteca.alterar('/'+recurso+'/'+form.dataset.id,dados,'PUT');
            else await biblioteca.criar(recurso,dados);fecharModal();avisar('Cadastro salvo com sucesso.');await navegar();
        } else if(form.id==='form-retirada'){
            await biblioteca.alterar('/reservas/'+form.dataset.id+'/retirar',dados);fecharModal();avisar('Empréstimo registrado.');await navegar();
        } else if(form.id==='form-senha'){
            await biblioteca.alterar('/minha-conta/senha',dados);form.reset();avisar('Senha alterada.');
        } else if(form.id==='form-telefone'){
            await biblioteca.criar('minha-conta/telefones',{usuarioId:sessao.usuario.id,numero:dados.numero});avisar('Telefone adicionado.');await navegar();
        }
    }catch(erro){if(erroCampo)erroCampo.textContent=mensagemErro(erro);else avisar(mensagemErro(erro),true);}
    finally{if(enviar){enviar.disabled=false;enviar.innerHTML=textoEnviar;}}
});
document.addEventListener('input',e=>{
    if(e.target.id==='busca-tabela'){
        const texto=e.target.value.toLocaleLowerCase('pt-BR');let encontrados=0;
        document.querySelectorAll('tbody tr').forEach(tr=>{tr.hidden=!tr.textContent.toLocaleLowerCase('pt-BR').includes(texto);if(!tr.hidden)encontrados++;});
        const vazio=document.querySelector('#sem-filtro');if(vazio)vazio.hidden=encontrados>0;
    }
});
document.addEventListener('change',e=>{if(e.target.id==='tipo-banco'){const grupo=document.querySelector('#dados-mysql');grupo.hidden=e.target.value!=='mysql';grupo.querySelectorAll('input').forEach(c=>c.disabled=grupo.hidden);}if(e.target.id==='tipo-exemplar')atualizarFormato();if(e.target.id==='so-disponiveis'){pagina=0;buscarCatalogo();}});
document.addEventListener('error',e=>{if(e.target.matches?.('.capa img'))e.target.classList.add('falhou');},true);
window.addEventListener('hashchange',navegar);
try {sessao=new Sessao(await autenticacao.eu());} catch(erro){if(erro.status!==401)avisar(mensagemErro(erro),true);}
await navegar();
