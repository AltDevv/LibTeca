// O catálogo apresenta uma edição digital; a gestão conserva os acessos individuais.
export function exemplaresCatalogo(exemplares) {
    const fisicos=exemplares.filter(e=>e.tipo!=='VIRTUAL');
    const digitais=exemplares.filter(e=>e.tipo==='VIRTUAL');
    if(!digitais.length)return fisicos;
    const prioridade=['DISPONIVEL','EMPRESTADO','RESERVADO','INDISPONIVEL'];
    const digital=[...digitais].sort((a,b)=>prioridade.indexOf(a.status)-prioridade.indexOf(b.status)||a.id-b.id)[0];
    return [...fisicos,digital];
}
