export function avisar(mensagem, erro=false) {
    const aviso=document.createElement('div'); aviso.className='toast'+(erro?' erro':''); aviso.textContent=mensagem;
    document.querySelector('#avisos').append(aviso); setTimeout(()=>aviso.remove(),6000);
}
export function mensagemErro(erro) { return erro.message || 'Não foi possível concluir. Tente novamente.'; }
