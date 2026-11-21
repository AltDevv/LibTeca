import { bancoTela } from './BancoTela.js';
import { esc } from '../mapper/VisualMapper.js';
export function configuracaoTela(info,sessao) {
    if(info.escolherBanco)return bancoTela();
    const primeira=!info.configurada;
    if(primeira && info.possuiAdministrador && !sessao.administrador) {
        return '<section class="setup"><h1>Configurar biblioteca</h1><p>Esta instalação já possui um administrador. Entre com essa conta para definir o nome e concluir a configuração.</p><a class="btn" href="#/login">Entrar como administrador →</a></section>';
    }
    const criar=primeira&&!info.possuiAdministrador;
    return '<section class="setup"><h1>'+(primeira?'Configurar biblioteca':'Minha biblioteca')+'</h1>'+
        '<form id="form-configuracao" data-instalar="'+criar+'"><div class="setup-section"><h2>Dados da biblioteca</h2><label>Nome da biblioteca<input name="nome" value="'+esc(primeira?'':info.nome)+'" placeholder="Ex.: Biblioteca da Escola Horizonte" maxlength="100" required autocomplete="organization"></label><label>Endereço do site <span class="muted">(opcional)</span><input type="url" name="site" value="'+esc(info.site)+'" placeholder="https://biblioteca.exemplo.com" maxlength="255"></label><p class="field-help">Opcional. Não registra domínio nem publica o site.</p></div>'+
        (criar?'<div class="setup-section"><h2>Conta do administrador</h2><label>Seu nome<input name="nomeAdministrador" maxlength="70" required autocomplete="name"></label><label>Email<input type="email" name="email" maxlength="100" required autocomplete="username"></label><div class="form-grid"><label>Crie uma senha<input type="password" name="senha" minlength="8" maxlength="60" required autocomplete="new-password"></label><label>Confirme a senha<input type="password" name="confirmacao" minlength="8" maxlength="60" required autocomplete="new-password"></label></div><p class="field-help">Use de 8 a 60 caracteres.</p></div><div class="setup-section"><h2>Confirme a instalação</h2><label>Código temporário<input name="codigo" maxlength="128" required autocomplete="off" spellcheck="false"></label><p class="field-help">No computador que iniciou o LibTeca, abra <code>data/instalacao.codigo</code> e copie o conteúdo. O código vale por 30 minutos. Se expirar, reinicie o backend para gerar outro.</p></div>':'')+
        '<p class="form-error" role="alert"></p><button type="submit" class="btn">'+(primeira?'Criar minha biblioteca →':'Salvar alterações')+'</button></form>'+
        (!primeira&&sessao.administrador?'<section class="setup-section" style="margin-top:32px"><h2>Administração</h2><div class="row-actions"><button class="btn secundario" data-acao="transferir-administracao">Transferir para um funcionário</button><button class="btn secundario perigo" data-acao="apagar-sistema">Apagar sistema</button></div></section>':'')+'</section>';
}
