@echo off
echo Preparando entorno y descargando dependencias si es necesario...
powershell -ExecutionPolicy Bypass -File "%~dp0run_backend.ps1"
pause
