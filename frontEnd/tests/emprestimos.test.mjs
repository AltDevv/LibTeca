import test from 'node:test';
import assert from 'node:assert/strict';
import { livroTela } from '../telas/LivroTela.js';
import { circulacaoTela } from '../telas/CirculacaoTela.js';
import { leitorTela } from '../telas/LeitorTela.js';

const livro={id:1,titulo:'Livro',autores:[],categorias:[],editora:{nome:'Editora'},numeroDePaginas:100,dataLancamento:'2020-05-10'};
const exemplar={id:2,tipo:'FISICO',status:'DISPONIVEL',livro};
const sessao={autenticado:true};
const pedido={id:3,exemplar,usuario:{nome:'Leitor'},status:'PENDENTE',dataEmprestimo:'2026-11-14',dataExpiracao:'2026-11-28',dataDevolucao:null};

test('reservar só aparece na falta do formato, com permissão e vaga na fila',()=>{
    const ocupado={...exemplar,status:'EMPRESTADO'};
    assert.match(livroTela(livro,[ocupado],sessao),/data-acao="reservar"/);
    assert.doesNotMatch(livroTela(livro,[ocupado,{...exemplar,id:9}],sessao),/data-acao="reservar"/);
    assert.doesNotMatch(livroTela({...livro,limiteReservas:0},[ocupado],sessao),/data-acao="reservar"/);
    assert.doesNotMatch(livroTela({...livro,limiteReservas:3,reservasEmAndamento:3},[ocupado],sessao),/data-acao="reservar"/);
    assert.doesNotMatch(livroTela(livro,[ocupado],sessao,[],[{status:'ATIVA',exemplar:ocupado}]),/data-acao="reservar"/);
    assert.match(livroTela(livro,[ocupado,{...exemplar,id:9,tipo:'VIRTUAL'}],sessao),/data-acao="reservar" data-id="2"/);
});

test('livro oferece empréstimo direto e leitor aguarda entrega',()=>{
    const disponivel=livroTela(livro,[exemplar],sessao);
    assert.match(disponivel,/data-acao="emprestar-fisico" data-id="2"/);
    assert.doesNotMatch(disponivel,/data-acao="reservar"/);
    const reservado={...exemplar,status:'RESERVADO'};
    const proprio=livroTela(livro,[reservado],sessao,[pedido]);
    assert.match(proprio,/Aguardando entrega pelo funcionário/);
    assert.doesNotMatch(proprio,/data-acao="receber-fisico"/);
    assert.doesNotMatch(livroTela(livro,[reservado],sessao),/receber-fisico|emprestar-fisico/);
    assert.doesNotMatch(livroTela(livro,[exemplar],{autenticado:false}),/emprestar-fisico/);
    assert.doesNotMatch(livroTela(livro,[{...exemplar,status:'EMPRESTADO'}],sessao,[{...pedido,status:'ATIVO'}]),/receber-fisico|emprestar-fisico/);
});

test('somente a gestão confirma entrega e o leitor pode cancelar',()=>{
    const pendente=circulacaoTela([pedido],'emprestimos');
    assert.match(pendente,/Aguardando entrega pelo funcionário/);
    assert.match(pendente,/Você já pode retirar o livro/);
    assert.doesNotMatch(pendente,/receber-fisico|entregar-gestao/);
    assert.match(pendente,/cancelar-emprestimo/);
    assert.doesNotMatch(pendente,/data-acao="devolver|Prazo:/);
    const cancelado=circulacaoTela([{...pedido,status:'CANCELADO'}],'emprestimos');
    assert.doesNotMatch(cancelado,/receber-fisico|cancelar-emprestimo|data-acao="devolver/);
    const gestao=circulacaoTela([pedido],'emprestimos',true);
    assert.doesNotMatch(gestao,/receber-fisico|devolver-gestao/);
    assert.match(gestao,/entregar-gestao/);
});

test('reserva física aguarda gestão e leitura digital fica na própria tela',()=>{
    const reserva={id:4,usuario:{nome:'Leitor'},exemplar:{...exemplar,status:'RESERVADO'},status:'DISPONIVEL_PARA_RETIRADA',dataReserva:'2026-11-14'};
    assert.doesNotMatch(livroTela(livro,[reserva.exemplar],sessao,[],[reserva]),/data-acao="receber-reserva"/);
    assert.match(circulacaoTela([reserva],'reservas'),/Aguardando entrega pelo funcionário/);
    assert.match(circulacaoTela([reserva],'reservas',true),/Confirmar entrega/);
    const leitor=leitorTela(pedido);
    assert.match(leitor,/<iframe[^>]+src="\/api\/arquivos\/leitura\/3#toolbar=0"/);
    assert.doesNotMatch(leitor,/target="_blank"|outra aba/);
});
