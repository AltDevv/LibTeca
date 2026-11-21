import { esc } from '../mapper/VisualMapper.js';
export function leitorTela(emprestimo) {
    return '<a class="back-link" href="#/emprestimos">← Meus empréstimos</a><div class="page-heading"><h1>'+esc(emprestimo.exemplar.livro.titulo)+'</h1></div><iframe class="leitor" title="Leitura do livro" src="/api/arquivos/leitura/'+Number(emprestimo.id)+'#toolbar=0"></iframe>';
}
