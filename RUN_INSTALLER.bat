@echo off
echo.
echo ========================================
echo   2048 Hexa Game - Installer
echo ========================================
echo.
echo Starting installer...
echo.
echo File: 2048-Hexa-Game-Setup.exe
echo Size: 93 MB
echo.
echo NOTE: Windows SmartScreen may show a warning.
echo       Click "More info" then "Run anyway"
echo.
echo Starting in 3 seconds...
timeout /t 3 /nobreak > nul

start "" "D:\Hexabrain\Other\compose-multiplatform-2048\2048-Hexa-Game-Setup.exe"

echo.
echo Installer launched!
echo Check for the installer window...
echo.
pause
