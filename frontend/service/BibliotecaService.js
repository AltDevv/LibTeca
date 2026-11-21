import { requisitar,todos,token } from '../repository/ApiRepository.js';
export const configuracao={
    escolherBanco:dados=>requisitar('/instalacao/banco',{method:'POST',body:dados}),
    informacoes:()=>requisitar('/biblioteca'),
    instalar:dados=>requisitar('/instalacao',{method:'POST',body:dados}),
    salvar:dados=>requisitar('/biblioteca',{method:'PUT',body:dados})
};
export const biblioteca={
    listar:todos,
    obter:(recurso,id)=>requisitar('/'+recurso+'/'+id),
    buscarLivros:params=>requisitar('/livros/buscar?'+params),
    livro:id=>requisitar('/livros/'+id),
    exemplares:id=>requisitar('/catalogo/'+id+'/exemplares'),
    meus:tipo=>requisitar('/minha-conta/'+tipo),
    criar:(recurso,dados)=>requisitar('/'+recurso,{method:'POST',body:dados}),
    alterar:(caminho,dados,method='PATCH')=>requisitar(caminho,{method,body:dados}),
    excluir:(recurso,id)=>requisitar('/'+recurso+'/'+id,{method:'DELETE'}),
    async enviar(arquivo,tipo) {const dados=new FormData();dados.set('arquivo',arquivo);dados.set('tipo',tipo);return requisitar('/arquivos',{method:'POST',body:dados});}
};
export const autenticacao={
    renovar:token,
    eu:()=>requisitar('/auth/eu'),
    async entrar(dados){await requisitar('/auth/entrar',{method:'POST',body:dados,form:true});await token();return this.eu();},
    async sair(){await requisitar('/auth/sair',{method:'POST'});await token();},
    cadastrar:dados=>requisitar('/auth/cadastro',{method:'POST',body:dados})
};
