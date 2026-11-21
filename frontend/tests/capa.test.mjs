import test from 'node:test';
import assert from 'node:assert/strict';
import { enquadramento } from '../service/CapaService.js';
import { capa } from '../mapper/VisualMapper.js';
import { cardsLivros } from '../telas/CatalogoTela.js';
import { livroTela } from '../telas/LivroTela.js';

test('enquadramento mantém proporção e permite escolher recorte ou imagem inteira',()=>{
    assert.deepEqual(enquadramento(1200,800),{x:-300,y:0,w:1200,h:800});
    assert.deepEqual(enquadramento(1200,800,1,50,50,true),{x:0,y:200,w:600,h:400});
    assert.deepEqual(enquadramento(600,800,2,100,25),{x:-600,y:-200,w:1200,h:1600});
});

test('retira somente a marca da capa e oferece exclusão apenas ao administrador nas telas',()=>{
    const livro={id:1,titulo:'Teste',autores:[{nome:'Autor'}],categorias:[],editora:{nome:'Editora'},numeroDePaginas:100};
    assert.ok(!capa(livro).includes('LIBTECA · ACERVO'));
    assert.ok(capa(livro).includes('Teste'));
    assert.ok(capa(livro).includes('Autor'));
    assert.ok(!cardsLivros({content:[livro]}).includes('data-acao="excluir-livro"'));
    assert.ok(cardsLivros({content:[livro]},true).includes('data-acao="excluir-livro"'));
    assert.ok(!livroTela(livro,[],{}).includes('data-acao="excluir-livro"'));
    assert.ok(livroTela(livro,[],{administrador:true}).includes('data-acao="excluir-livro"'));
});
