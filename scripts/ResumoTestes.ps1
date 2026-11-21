param([Parameter(Mandatory=$true)][string]$Pasta,[string]$Projeto,[int]$Resultado,[double]$Duracao)
$ErrorActionPreference = 'Stop'
function Esc($valor) { [System.Net.WebUtility]::HtmlEncode([string]$valor) }
$itens = @()
foreach ($arquivo in Get-ChildItem -LiteralPath (Join-Path $Pasta 'xml') -Filter 'TEST-*.xml') {
    $xml = New-Object System.Xml.XmlDocument
    $xml.XmlResolver = $null
    $xml.Load($arquivo.FullName)
    foreach ($caso in $xml.SelectNodes('//testcase')) {
        $status = 'APROVADO'; $detalhe = ''
        foreach ($tipo in @('skipped','failure','error')) {
            $no = $caso.SelectSingleNode($tipo)
            if ($null -ne $no) {
                $status = @{skipped='IGNORADO';failure='FALHOU';error='ERRO'}[$tipo]
                $detalhe = $no.GetAttribute('message') + "`n" + $no.InnerText
            }
        }
        $nome = $caso.GetAttribute('name')
        $titulo = [regex]::Replace($nome,'(?<=[a-zá-ú])(?=[A-ZÁ-Ú])',' ')
        $tempo = $caso.GetAttribute('time')
        $evidencia = Join-Path $Pasta "playwright/$nome"
        $itens += [pscustomobject]@{Status=$status;Nome=$titulo;Metodo=$nome;Classe=$caso.GetAttribute('classname');Tempo=$tempo;Detalhe=$detalhe.Trim();Imagem=(Test-Path -LiteralPath (Join-Path $evidencia 'tela.png'));Trace=(Test-Path -LiteralPath (Join-Path $evidencia 'trace.zip'))}
    }
}
$aprovados = @($itens | Where-Object Status -eq 'APROVADO').Count
$falhas = @($itens | Where-Object Status -eq 'FALHOU').Count
$erros = @($itens | Where-Object Status -eq 'ERRO').Count
$ignorados = @($itens | Where-Object Status -eq 'IGNORADO').Count
$estado = if ($Resultado -ne 0) {'EXECUÇÃO COM FALHA'} elseif ($itens.Count -eq 0) {'NENHUM TESTE REGISTRADO'} elseif ($falhas+$erros -gt 0) {'TESTES COM FALHA'} else {'EXECUÇÃO CONCLUÍDA'}
$linhas = @('', "LIBTECA - $Projeto - $estado", ('Duração total: {0:N1}s | Código Maven: {1}' -f $Duracao,$Resultado), "Total: $($itens.Count) | Aprovados: $aprovados | Falhas: $falhas | Erros: $erros | Ignorados: $ignorados", '')
$corpo = ''
foreach ($grupo in $itens | Group-Object Classe) {
    $linhas += $grupo.Name
    $corpo += '<h2>'+(Esc $grupo.Name)+'</h2><table><thead><tr><th>Resultado</th><th>Teste</th><th>Tempo</th><th>Detalhes</th></tr></thead><tbody>'
    foreach ($item in $grupo.Group) {
        $linhas += '  [{0}] {1} ({2}s)' -f $item.Status,$item.Nome,$item.Tempo
        if ($item.Detalhe) { $linhas += '    '+($item.Detalhe -split "`r?`n")[0] }
        $detalhes = ''
        if ($item.Detalhe) { $detalhes += '<details><summary>Ver motivo</summary><pre>'+(Esc $item.Detalhe)+'</pre></details>' }
        $caminho = 'playwright/'+[Uri]::EscapeDataString($item.Metodo)
        if ($item.Imagem) { $detalhes += '<a href="'+$caminho+'/tela.png">Imagem final</a> ' }
        if ($item.Trace) { $detalhes += '<a href="'+$caminho+'/trace.zip">Trace</a>' }
        $corpo += '<tr><td class="'+$item.Status+'">'+$item.Status+'</td><td>'+(Esc $item.Nome)+'</td><td>'+(Esc $item.Tempo)+'s</td><td>'+$detalhes+'</td></tr>'
    }
    $corpo += '</tbody></table>'
}
if ($itens.Count -eq 0) { $linhas += 'Não há resultados desta execução. Confira maven.log: a compilação pode ter falhado antes dos testes, ou o comando não executou testes.' }
$linhas += @('', 'Ignorado não significa aprovado. O motivo aparece nos detalhes quando informado pelo executor.', "Relatório HTML: $(Join-Path $Pasta 'index.html')", "Log completo: $(Join-Path $Pasta 'maven.log')")
$linhas | ForEach-Object { Write-Host $_ }
$linhas | Set-Content -LiteralPath (Join-Path $Pasta 'resumo.txt') -Encoding UTF8
$html = '<!doctype html><html lang="pt-BR"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>LibTeca — testes '+(Esc $Projeto)+'</title><style>body{font:16px system-ui;margin:32px auto;padding:0 20px;max-width:1200px;color:#183a36;background:#f5f8f7}table{width:100%;border-collapse:collapse;background:white}td,th{text-align:left;padding:12px;border-bottom:1px solid #ddd}h2{font-size:18px;margin-top:32px;overflow-wrap:anywhere}.APROVADO{color:#176547}.FALHOU,.ERRO{color:#b12332}.IGNORADO{color:#805600}pre{white-space:pre-wrap;overflow-wrap:anywhere;max-height:400px;overflow:auto}a{color:#12665b}td{overflow-wrap:anywhere}@media(max-width:600px){body{padding:0 8px}td,th{padding:7px;font-size:13px}}</style><h1>'+(Esc $estado)+'</h1><p>'+(Esc $Projeto)+' · '+(Get-Date -Format 'dd/MM/yyyy HH:mm:ss')+'</p><p>'+ (Esc $linhas[2])+'</p><p>'+(Esc $linhas[3])+'</p><p>Ignorados não contam como aprovados. <a href="maven.log">Log completo</a> · <a href="resumo.txt">Resumo em texto</a></p>'+$corpo
if ($itens.Count -eq 0) { $html += '<p>Nenhum teste foi registrado nesta execução. Consulte o log completo.</p>' }
$html += '</html>'
[System.IO.File]::WriteAllText((Join-Path $Pasta 'index.html'),$html,[System.Text.UTF8Encoding]::new($false))