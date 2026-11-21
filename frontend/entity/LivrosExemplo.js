// Cartões de apresentação: não são registros da API.
export function exemplosVisiveis(info,storage) {
    if(info.acervoIniciado) return [];
    let removidos=[];
    try { const dados=JSON.parse(storage.getItem('libteca.exemplos.removidos')||'[]');if(Array.isArray(dados))removidos=dados; } catch {}
    return [1,2,3].filter(numero=>!removidos.includes(numero));
}
export function removerExemplo(numero,storage) {
    const visiveis=exemplosVisiveis({acervoIniciado:false},storage).filter(n=>n!==numero);
    storage.setItem('libteca.exemplos.removidos',JSON.stringify([1,2,3].filter(n=>!visiveis.includes(n))));
}
