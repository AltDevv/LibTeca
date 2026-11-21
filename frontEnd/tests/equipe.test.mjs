import test from 'node:test';
import assert from 'node:assert/strict';
import {Sessao} from '../entity/Sessao.js';
import {podeAbrir} from '../specification/Permissoes.js';
import {estrutura} from '../telas/EstruturaTela.js';
import {tabelaTela} from '../telas/GestaoTela.js';
import {formularioTela} from '../telas/FormularioTela.js';

test('funcionário não acessa equipe nem pode excluir funcionários',()=>{
    const sessao=new Sessao({usuario:{id:1,nome:'Atendente',role:'FUNCIONARIO'}});
    assert.equal(podeAbrir('equipe',sessao),false);
    assert.equal(podeAbrir('equipe',new Sessao({usuario:{role:'USER'}})),false);
    assert.doesNotMatch(estrutura(sessao,'catalogo',{nome:'Biblioteca'}),/href="#\/equipe"/);
    const html=tabelaTela('funcionarios',[{id:1,usuario:sessao.usuario,ativo:true}],false,undefined,sessao);
    assert.doesNotMatch(html,/data-acao="excluir"|alternar-funcionario|Cadastrar funcionário|Solicitar funcionário/);
    assert.doesNotMatch(tabelaTela('usuarios',[sessao.usuario],false,undefined,sessao),/data-acao="excluir"/);
});
test('administrador cadastra e gerencia a equipe diretamente',()=>{
    const sessao=new Sessao({usuario:{id:1,nome:'Admin',role:'ADMIN'}});
    assert.equal(podeAbrir('equipe',sessao),true);
    assert.match(estrutura(sessao,'equipe',{nome:'Biblioteca'}),/href="#\/equipe"/);
    const item={id:7,usuario:{id:2,nome:'Atendente',role:'FUNCIONARIO'},ativo:true,dataAdmissao:'2026-01-10'};
    const html=tabelaTela('funcionarios',[item],false,undefined,sessao);
    assert.match(html,/Cadastrar funcionário/);
    assert.match(html,/data-acao="excluir"/);
    assert.match(html,/alternar-funcionario/);
    assert.doesNotMatch(html,/Solicitar funcionário|aprovar-funcionario/);
    assert.match(formularioTela('funcionarios',{usuarios:[]}),/Cadastrar funcionário/);
});
