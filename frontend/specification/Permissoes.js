export function podeAbrir(rota,sessao) {
    if(['catalogo','livro','login','cadastro','configuracao'].includes(rota)) return true;
    if(rota==='equipe') return sessao.administrador;
    if(['painel','acervo','exemplares','usuarios','cadastros','circulacao','fila'].includes(rota)) return sessao.gestao;
    return sessao.autenticado;
}
