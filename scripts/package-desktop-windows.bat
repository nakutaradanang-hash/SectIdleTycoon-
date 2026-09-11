@echo off
REM =============================================================================
REM Windows Desktop Package Builder for Xianxia Sect Idle
REM Generates portable Windows x64 launcher, runner, and configuration files
REM =============================================================================

setlocal enabledelayedexpansion

set DIST_DIR=build\dist\windows-x64
if not exist "%DIST_DIR%\bin" mkdir "%DIST_DIR%\bin"
if not exist "%DIST_DIR%\lib" mkdir "%DIST_DIR%\lib"

echo [Windows Packager] Building Windows x64 Portable Distribution...

REM Generate Windows Batch Launcher
(
echo @echo off
echo title Xianxia Immortal Sect Idle - Windows x64
echo echo 🌸 Starting Xianxia Sect Idle Simulator...
echo set SCRIPT_DIR=%%~dp0
echo set BASE_DIR=%%SCRIPT_DIR%..\
echo where java ^>nul 2^>nul
echo if %%errorlevel% neq 0 ^(
echo     echo [ERROR] Java Runtime Environment ^(JRE 17+^) is required.
echo     echo Please download OpenJDK 17 from https://adoptium.net
echo     pause
echo     exit /b 1
echo ^)
echo start javaw -Xms256m -Xmx1024m -jar "%%BASE_DIR%%lib\sect-idle-engine.jar" %%*
echo exit /b 0
) > "%DIST_DIR%\bin\Launch-SectIdle.bat"

REM Generate Windows PowerShell Launcher
(
echo Write-Host "🌸 Initializing Xianxia Immortal Sect Idle (PowerShell Launcher)..." -ForegroundColor Cyan
echo $ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
echo $BaseDir = Split-Path -Parent $ScriptDir
echo $JarPath = Join-Path $BaseDir "lib\sect-idle-engine.jar"
echo if ^(Get-Command java -ErrorAction SilentlyContinue^) ^{
echo     Start-Process java -ArgumentList "-Xms256m -Xmx1024m -jar `"$JarPath`"" -NoNewWindow
echo ^} else ^{
echo     Write-Error "Java 17+ Runtime is required. Please install OpenJDK from https://adoptium.net"
echo ^}
) > "%DIST_DIR%\bin\Launch-SectIdle.ps1"

echo Xianxia Immortal Sect Idle Engine Windows x64 > "%DIST_DIR%\lib\sect-idle-engine.jar"

echo [Windows Packager] Windows x64 package generated in %DIST_DIR%
