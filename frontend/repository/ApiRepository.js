import { API_BASE } from '../config/ApiConfig.js';
let csrf;
export async function token() {
    const resposta=await fetch(API_BASE+'/auth/csrf',{credentials:'same-origin'});
    if(!resposta.ok) throw new Error('Não foi possível iniciar a sessão. Verifique o servidor.');
    csrf=await resposta.json(); return csrf;
}
export async function requisitar(caminho,{method='GET',body,form=false}={}) {
    const headers={};
    if(!['GET','HEAD'].includes(method)) { if(!csrf) await token(); headers[csrf.headerName]=csrf.token; }
    let content=body;
    if(body && !(body instanceof FormData)) {
        headers['Content-Type']=form?'application/x-www-form-urlencoded':'application/json';
        content=form?new URLSearchParams(body):JSON.stringify(body);
    }
    let response;
    try { response=await fetch(API_BASE+caminho,{method,headers,body:content,credentials:'same-origin'}); }
    catch { throw new Error('Sem conexão com o servidor. Confira sua internet e tente novamente.'); }
    if(response.status===204) return null;
    const data=await response.json().catch(()=>null);
    if(!response.ok) {
        if(response.status===403) csrf=null;
        const erro=new Error(data?.campos?Object.values(data.campos).join(' · '):data?.mensagem||data?.detail||'Não foi possível concluir esta operação.');
        erro.status=response.status; throw erro;
    }
    return data;
}
export async function todos(recurso) {
    const primeira=await requisitar('/'+recurso+'?size=50&sort=id,desc');
    if(Array.isArray(primeira)) return primeira;
    let resultado=[...primeira.content];
    for(let pagina=1;pagina<primeira.totalPages;pagina++) {
        const outra=await requisitar('/'+recurso+'?size=50&sort=id,desc&page='+pagina);
        resultado.push(...outra.content);
    }
    return resultado;
}
