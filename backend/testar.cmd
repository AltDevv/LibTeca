@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0..\scripts\Testar.ps1" backend %*
exit /b %ERRORLEVEL%
