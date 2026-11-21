import test from 'node:test';
import assert from 'node:assert/strict';
import { dadosFormulario } from '../dto/FormularioRequest.js';

test('seleções de autores e categorias viram listas de IDs, sem enviar campos do outro formato',()=>{
    const campo=(name,value,extra={})=>({name,value,type:'text',dataset:{},...extra});
    const dados=dadosFormulario({elements:[
        campo('titulo','  Livro de teste  '),
        campo('autoresIds','2',{type:'checkbox',checked:true,dataset:{listaNumero:''}}),
        campo('autoresIds','4',{type:'checkbox',checked:false,dataset:{listaNumero:''}}),
        campo('autoresIds','7',{type:'checkbox',checked:true,dataset:{listaNumero:''}}),
        campo('categoriasIds','3',{type:'checkbox',checked:true,dataset:{listaNumero:''}}),
        campo('codigoPatrimonio','FIS-01',{disabled:true}),
        campo('caminhoArquivo','livros/teste.pdf'),
        campo('uploadLivro','arquivo.pdf',{type:'file'}),
        campo('ativo','on',{type:'checkbox',checked:false})
    ]});
    assert.deepEqual(dados,{titulo:'Livro de teste',autoresIds:[2,7],categoriasIds:[3],caminhoArquivo:'livros/teste.pdf',ativo:false});
});
