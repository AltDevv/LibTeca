export function filtroLivro(form, pagina=0) {
    const params=new URLSearchParams(new FormData(form));
    for(const [chave,valor] of [...params]) if(!valor) params.delete(chave);
    params.set('page',pagina); params.set('size',12); params.set('sort','titulo,asc'); return params;
}
