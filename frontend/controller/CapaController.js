import { enquadramento } from '../service/CapaService.js';
const estados=new WeakMap();

export function prepararCapa(form) {
    const campo=form?.elements.uploadCapa;if(!campo)return;
    const painel=form.querySelector('.ajuste-capa'),canvas=painel.querySelector('canvas');
    const status=form.querySelector('[data-capa-status]');
    const controle=nome=>painel.querySelector('[data-capa="'+nome+'"]');
    const estado={imagem:null,erro:null,carregamento:null,versao:0};estados.set(form,estado);
    const valores=()=>enquadramento(estado.imagem.naturalWidth,estado.imagem.naturalHeight,Number(controle('zoom').value),Number(controle('x').value),Number(controle('y').value),controle('modo').value==='inteira');
    const desenhar=()=>{if(!estado.imagem)return;const r=valores(),ctx=canvas.getContext('2d');ctx.fillStyle='#fff';ctx.fillRect(0,0,600,800);ctx.drawImage(estado.imagem,r.x,r.y,r.w,r.h);};
    const restaurar=()=>{controle('zoom').value=1;controle('x').value=50;controle('y').value=50;desenhar();};
    painel.addEventListener('input',desenhar);
    controle('modo').addEventListener('change',restaurar);
    controle('restaurar').addEventListener('click',()=>{controle('modo').value='preencher';restaurar();});
    let arrasto=null;
    canvas.addEventListener('pointerdown',e=>{if(!estado.imagem)return;e.preventDefault();canvas.setPointerCapture(e.pointerId);arrasto={x:e.clientX,y:e.clientY,h:Number(controle('x').value),v:Number(controle('y').value)};});
    canvas.addEventListener('pointermove',e=>{if(!arrasto||!estado.imagem)return;const r=valores(),fator=600/canvas.getBoundingClientRect().width;const limitar=n=>Math.max(0,Math.min(100,n));
        if(Math.abs(600-r.w)>1)controle('x').value=limitar(arrasto.h+(e.clientX-arrasto.x)*fator/(600-r.w)*100);
        if(Math.abs(800-r.h)>1)controle('y').value=limitar(arrasto.v+(e.clientY-arrasto.y)*fator/(800-r.h)*100);desenhar();});
    canvas.addEventListener('pointerup',()=>arrasto=null);canvas.addEventListener('pointercancel',()=>arrasto=null);canvas.addEventListener('lostpointercapture',()=>arrasto=null);
    campo.addEventListener('change',()=>{
        const versao=++estado.versao,arquivo=campo.files[0];estado.imagem=null;estado.erro=null;painel.hidden=true;form.elements.capa.disabled=!!arquivo;status.textContent=arquivo?'Carregando imagem…':'';
        estado.carregamento=(async()=>{
            if(!arquivo)return;
            let url;
            try {
                if(!['image/png','image/jpeg'].includes(arquivo.type))throw new Error('Escolha uma imagem JPG ou PNG.');
                if(arquivo.size>20*1024*1024)throw new Error('A imagem deve ter até 20 MB.');
                const imagem=new Image();url=URL.createObjectURL(arquivo);imagem.src=url;await imagem.decode();
                if(versao!==estado.versao)return;
                if(imagem.naturalWidth*imagem.naturalHeight>40000000)throw new Error('Escolha uma imagem com até 40 megapixels.');
                estado.imagem=imagem;controle('modo').value='preencher';restaurar();painel.hidden=false;status.textContent='';
            } catch(erro) {if(versao===estado.versao){estado.erro=erro.message==='The source image cannot be decoded.'?'Não foi possível abrir a imagem. Escolha outro arquivo.':erro.message;status.textContent=estado.erro;}}
            finally {if(url)URL.revokeObjectURL(url);}
        })();
    });
}

export async function arquivoCapa(form) {
    if(!form.elements.uploadCapa?.files[0])return null;
    const estado=estados.get(form);await estado?.carregamento;
    if(!estado?.imagem||estado.erro)throw new Error(estado?.erro||'Aguarde a prévia da capa carregar.');
    const canvas=form.querySelector('.ajuste-capa canvas');
    const blob=await new Promise(resolve=>canvas.toBlob(resolve,'image/jpeg',0.92));
    if(!blob)throw new Error('Não foi possível preparar a capa. Selecione a imagem novamente.');
    return new File([blob],'capa.jpg',{type:'image/jpeg'});
}
