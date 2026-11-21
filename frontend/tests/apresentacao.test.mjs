import test from 'node:test';
import assert from 'node:assert/strict';
import { exemplosVisiveis,removerExemplo } from '../entity/LivrosExemplo.js';
import { estrutura } from '../telas/EstruturaTela.js';
import { catalogoTela,cardsExemplo } from '../telas/CatalogoTela.js';
import { Sessao } from '../entity/Sessao.js';
test('exemplos são removíveis e nunca voltam depois do primeiro livro',()=>{
    const valores=new Map();const storage={getItem:k=>valores.get(k),setItem:(k,v)=>valores.set(k,v)};
    assert.deepEqual(exemplosVisiveis({acervoIniciado:false},storage),[1,2,3]);
    removerExemplo(2,storage);
    assert.deepEqual(exemplosVisiveis({acervoIniciado:false},storage),[1,3]);
    assert.deepEqual(exemplosVisiveis({acervoIniciado:true},storage),[]);
    valores.clear();assert.deepEqual(exemplosVisiveis({acervoIniciado:true},storage),[]);
    assert.equal((cardsExemplo([1,2,3],true).match(/<article/g)||[]).length,3);
    assert.ok(!cardsExemplo([1],false).includes('href='));
});
test('nome da biblioteca é mostrado como texto sem injetar HTML',()=>{
    const info={nome:'Biblioteca <img src=x onerror=alert(1)>',configurada:true};
    const shell=estrutura(new Sessao(),'catalogo',info),catalogo=catalogoTela([],false,info.nome);
    assert.ok(shell.includes('Biblioteca &lt;img'));
    assert.ok(catalogo.includes('Biblioteca &lt;img'));
    assert.ok(!shell.includes('<img src=x'));
});
