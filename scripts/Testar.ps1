# Compatível com Windows PowerShell 5.1. Os argumentos seguintes são repassados ao Maven.
$projeto = $args[0]
if ($projeto -notin @('frontEnd','backend')) { throw 'Escolha frontEnd ou backend.' }
$argumentos = @($args | Select-Object -Skip 1)
if ($argumentos.Count -eq 0) { $argumentos = @('test') }
$raiz = Split-Path $PSScriptRoot -Parent
$execucao = (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0,6)
$pasta = Join-Path $raiz "$projeto/target/execucoes/$execucao"
$relatorios = Join-Path $pasta 'xml'
New-Item -ItemType Directory -Path $relatorios -Force | Out-Null
$opcoes = @("-Dtestes.relatorios=$relatorios")
if ($projeto -eq 'frontEnd') {
    $opcoes += @('-Pfront-playwright', "-Dtestes.evidencias=$(Join-Path $pasta 'playwright')")
}
$inicio = Get-Date
Push-Location (Join-Path $raiz 'backend')
try {
    & '.\mvnw.cmd' @opcoes @argumentos 2>&1 | Tee-Object -FilePath (Join-Path $pasta 'maven.log') | ForEach-Object { Write-Host $_ }
    $resultado = $LASTEXITCODE
} finally { Pop-Location }
if ($null -eq $resultado) { $resultado = 1 }
$duracao = ((Get-Date)-$inicio).TotalSeconds
& (Join-Path $PSScriptRoot 'ResumoTestes.ps1') -Pasta $pasta -Projeto $projeto -Resultado $resultado -Duracao $duracao
# Uma falha no Maven continua sendo uma falha para o terminal/CI.
exit $resultado