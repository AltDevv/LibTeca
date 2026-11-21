import test from 'node:test';
import assert from 'node:assert/strict';
import { tabelaTela } from '../telas/GestaoTela.js';
import { configuracaoTela } from '../telas/ConfiguracaoTela.js';
import { administracaoTela } from '../telas/AdministracaoTela.js';
test('administradores não têm excluir ou desativar nas listas',()=>{
    for(const role of ['ADMIN','ADMIM']){
        const usuario={id:5,nome:'Admin',email:'admin@teste.local',role};
        const funcionario={id:8,usuario,ativo:true,dataAdmissao:'2026-01-01'};
        for(const [recurso,itens] of [['usuarios',[usuario]],['funcionarios',[funcionario]]]){
            const html=tabelaTela(recurso,itens,false,'fisicos',{administrador:false,usuario:{id:9}});
            assert.doesNotMatch(html,/data-acao="excluir"|alternar-funcionario|Gerenciar administração/);
            assert.match(tabelaTela(recurso,itens,false,'fisicos',{administrador:true,usuario}),/Gerenciar administração/);
        }
    }
});
test('configuração oferece transferência e exclusão apenas ao administrador',()=>{
    const info={configurada:true,nome:'Biblioteca'};
    assert.match(configuracaoTela(info,{administrador:true}),/apagar-sistema/);
    assert.match(configuracaoTela(info,{administrador:true}),/transferir-administracao/);
    assert.doesNotMatch(configuracaoTela(info,{administrador:false}),/apagar-sistema|transferir-administracao/);
    const modal=administracaoTela('apagar-sistema','<Biblioteca>');
    assert.match(modal,/nomeBiblioteca/);assert.match(modal,/senhaAtual/);assert.match(modal,/&lt;Biblioteca&gt;/);
    assert.match(administracaoTela('transferir-administracao','Biblioteca'),/disabled/);
});
