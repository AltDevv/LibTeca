import test from 'node:test';
import assert from 'node:assert/strict';
import { exemplaresCatalogo } from '../mapper/ExemplarMapper.js';
import { livroTela } from '../telas/LivroTela.js';

test('catálogo mantém físicos e mostra um único digital disponível',()=>{
    const exemplares=[{id:1,tipo:'VIRTUAL',status:'EMPRESTADO'},
        {id:2,tipo:'FISICO',status:'DISPONIVEL'},
        {id:3,tipo:'VIRTUAL',status:'DISPONIVEL'},
        {id:4,tipo:'VIRTUAL',status:'INDISPONIVEL'}];
    assert.deepEqual(exemplaresCatalogo(exemplares).map(e=>e.id),[2,3]);
    assert.equal(exemplares.length,4);
    const html=livroTela({id:1,titulo:'Livro',autores:[],categorias:[],editora:{nome:'Editora'},numeroDePaginas:100,dataLancamento:'2020-05-10'},exemplares,{autenticado:true});
    assert.equal((html.match(/Edição digital/g)||[]).length,1);
    assert.match(html,/data-acao="emprestar-digital" data-id="3"/);
});

test('digital sem PDF não oferece leitura e catálogo sem exemplares permanece vazio',()=>{
    assert.deepEqual(exemplaresCatalogo([]),[]);
    const html=livroTela({id:1,titulo:'Livro',autores:[],categorias:[],editora:{nome:'Editora'}},
        [{id:1,tipo:'VIRTUAL',status:'INDISPONIVEL'},{id:2,tipo:'VIRTUAL',status:'INDISPONIVEL'}],{autenticado:true});
    assert.equal((html.match(/Edição digital/g)||[]).length,1);
    assert.doesNotMatch(html,/emprestar-digital/);
});
import { tabelaTela } from '../telas/GestaoTela.js';
test('gestão separa físicos e virtuais, mantendo IDs reais nas ações',()=>{
    const itens=[{id:10,tipo:'FISICO',livro:{titulo:'Papel'},status:'DISPONIVEL'},
        {id:30,tipo:'VIRTUAL',livro:{titulo:'Digital'},status:'DISPONIVEL',emprestimosAtivos:7,totalEmprestimos:12}];
    const virtual=tabelaTela('exemplares',itens,false,'virtuais');
    assert.match(virtual,/V001/);assert.match(virtual,/data-id="30"/);
    assert.match(virtual,/Leituras ativas/);assert.match(virtual,/>7<\/td>/);assert.match(virtual,/>12<\/td>/);
    assert.doesNotMatch(virtual,/Papel/);
    const fisico=tabelaTela('exemplares',itens);
    assert.match(fisico,/#10/);assert.doesNotMatch(fisico,/data-id="30"/);
});
