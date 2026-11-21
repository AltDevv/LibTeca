export class Sessao {
    constructor(dados) { this.usuario = dados?.usuario || null; this.funcionarioId = dados?.funcionarioId || null; }
    get autenticado() { return !!this.usuario; }
    get administrador() { return ['ADMIN','ADMIM'].includes(this.usuario?.role); }
    get gestao() { return this.administrador || this.usuario?.role === 'FUNCIONARIO'; }
}
