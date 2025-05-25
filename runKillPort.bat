@echo off
setlocal

:: Cambiar al directorio donde reside el script (IMPORTANTE)
cd /d "%~dp0"

:: 1) Verificar permisos de administrador
fsutil dirty query %systemdrive% >nul 2>&1
if not "%errorlevel%"=="0" (
    echo Solicitando permisos de administrador...
    powershell -Command "Start-Process -FilePath '%~f0' -Verb RunAs -WorkingDirectory \"%cd%\""
    exit /b
)

:: 2) Si se recibe un directorio como argumento, cambiar a él
if not "%~1"=="" (
    cd /d "%~1" 2>nul || (
        echo Error: Directorio "%~1" no encontrado
        pause
        exit /b
    )
)

echo ================================
echo Compilando KillPort.java...
javac KillPort.java
if errorlevel 1 (
    echo Error de compilacion. Revisa:
    echo - ¿El archivo existe en este directorio?
    echo - ¿Tienes JDK instalado y en el PATH?
    pause
    exit /b
)

echo ================================
echo Ejecutando KillPort...
java -cp . KillPort
if errorlevel 1 (
    echo Error: Fallo al ejecutar KillPort
) else (
    echo KillPort finalizó correctamente.
)

echo ================================
pause
endlocal