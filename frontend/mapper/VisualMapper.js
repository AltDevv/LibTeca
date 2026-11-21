import { rotulos } from '../enums/Status.js';
export const esc = v => String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
export function data(v) { return v ? new Intl.DateTimeFormat('pt-BR').format(new Date(v+'T12:00:00')) : '—'; }
export function badge(v) { return '<span class="badge '+esc(v?.toLowerCase())+'">'+esc(rotulos[v]||v)+'</span>'; }
export function capa(livro,classe='') {
    const cores=['jade','terracota','azul','areia','ameixa']; const cor=cores[(livro.id||0)%cores.length];
    const ref=livro.capa;
    // Imagens somente por referências HTTP(S) ou caminhos locais; nunca HTML do banco.
    const seguro=ref && (/^https?:\/\//i.test(ref) || /^\/?capas\//.test(ref));
    return '<div class="capa '+cor+' '+classe+'">'+(seguro?'<img src="'+esc(ref)+'" alt="Capa de '+esc(livro.titulo)+'" loading="lazy">':'')+
        '<div class="capa-texto"><strong>'+esc(livro.titulo)+'</strong><i></i><small>'+esc(livro.autores?.map(a=>a.nome).join(' & '))+'</small></div></div>';
}
export function vazio(titulo,texto,acao='') { return '<div class="vazio"><span class="vazio-icone">'+icone('vazio')+'</span><h3>'+esc(titulo)+'</h3><p>'+esc(texto)+'</p>'+acao+'</div>'; }
export function botao(texto,acao,id='',classe='btn secundario') { return '<button class="'+classe+'" data-acao="'+acao+'" data-id="'+id+'">'+esc(texto)+'</button>'; }

const desenhos={
    livro:'<path d="M4 4h6a3 3 0 0 1 3 3v14a4 4 0 0 0-4-2H4z"/><path d="M13 7a3 3 0 0 1 3-3h4v15h-3a4 4 0 0 0-4 2"/>',
    catalogo:'<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>',
    painel:'<path d="M3 12h4v9H3zM10 7h4v14h-4zM17 3h4v18h-4z"/>',
    exemplares:'<path d="M7 3h13v14H7zM4 7H3v14h13v-1"/>',
    usuarios:'<circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M16 5a3 3 0 0 1 0 6M18 15a5 5 0 0 1 3 5"/>',
    perfil:'<circle cx="12" cy="8" r="4"/><path d="M4 21v-2a8 8 0 0 1 16 0v2"/>',
    reserva:'<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4m10-4v4M3 11h18m-14 5h4"/>',
    emprestimo:'<path d="M3 7h17m-4-4 4 4-4 4M21 17H4m4-4-4 4 4 4"/>',
    cadastros:'<path d="M9 5h12M9 12h12M9 19h12M3 5h1M3 12h1M3 19h1"/>',
    config:'<path d="M4 7h16M4 17h16"/><circle cx="9" cy="7" r="3"/><circle cx="16" cy="17" r="3"/>',
    busca:'<circle cx="10.5" cy="10.5" r="7"/><path d="m16 16 5 5"/>',
    mais:'<path d="M12 5v14M5 12h14"/>',
    seta:'<path d="M5 12h14m-6-6 6 6-6 6"/>',
    menu:'<path d="M4 6h16M4 12h16M4 18h16"/>',
    fechar:'<path d="m6 6 12 12M6 18 18 6"/>',
    autores:'<path d="m4 16 12-12 4 4L8 20H4zM13 7l4 4"/>',
    categorias:'<path d="M3 3h8l10 10-8 8L3 11z"/><circle cx="7.5" cy="7.5" r="1"/>',
    editoras:'<path d="M5 21V3h14v18M3 21h18M9 7h1m4 0h1m-6 4h1m4 0h1m-6 4h1m4 0h1"/>',
    vazio:'<path d="M3 9 7 3h10l4 6v12H3zM3 9h5l2 4h4l2-4h5"/>'
};
export function icone(nome) {return '<svg class="icone" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">'+(desenhos[nome]||desenhos.livro)+'</svg>';}
