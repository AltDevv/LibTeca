import test from 'node:test';
import assert from 'node:assert/strict';
import { dadosFormulario } from '../dto/FormularioRequest.js';
import { formularioTela } from '../telas/FormularioTela.js';

test('livro permite definir limite e desativar reservas sem enviar campo extra',()=>{
    assert.match(formularioTela('livros'),/name="limiteReservas"[^>]+value="3"/);
    const permitir={name:'permiteReserva',type:'checkbox',checked:false,dataset:{}};
    const limite={name:'limiteReservas',type:'number',value:'5',dataset:{},disabled:true};
    assert.deepEqual(dadosFormulario({elements:[permitir,limite]}),{limiteReservas:0});
    permitir.checked=true;limite.disabled=false;
    assert.deepEqual(dadosFormulario({elements:[permitir,limite]}),{limiteReservas:5});
});

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
