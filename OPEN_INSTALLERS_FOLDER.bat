@echo off
echo.
echo ========================================
echo   Opening Installers Folder...
echo ========================================
echo.
echo Location: D:\Hexabrain\Other\compose-multiplatform-2048
echo.
echo Look for these files:
echo   - 2048-Hexa-Game-Setup.exe (93 MB)
echo   - 2048-Hexa-Game-Setup.msi (93 MB)
echo.
echo Opening folder in 2 seconds...
timeout /t 2 /nobreak > nul
explorer.exe "D:\Hexabrain\Other\compose-multiplatform-2048"
echo.
echo Folder opened! Check Windows Explorer.
echo.
pause
