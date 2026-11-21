export function dadosFormulario(form) {
    const dados={};
    for(const campo of form.elements) {
        if(!campo.name || campo.disabled || campo.type==='file') continue;
        if(campo.dataset.listaNumero!==undefined){dados[campo.name]??=[];if(campo.checked)dados[campo.name].push(Number(campo.value));}
        else if(campo.multiple) dados[campo.name]=[...campo.selectedOptions].map(o=>Number(o.value));
        else if(campo.type==='checkbox') dados[campo.name]=campo.checked;
        else if(campo.dataset.numero!==undefined || campo.type==='number') dados[campo.name]=campo.value?Number(campo.value):null;
        else dados[campo.name]=(campo.type==='password'?campo.value:campo.value.trim()) || null;
    }
    if ('permiteReserva' in dados) {
        if (!dados.permiteReserva) dados.limiteReservas=0;
        delete dados.permiteReserva;
    }
    return dados;
}
