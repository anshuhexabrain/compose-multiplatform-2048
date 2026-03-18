@echo off
echo ========================================
echo  COMPLETE UNINSTALL - Remove ALL Traces
echo ========================================
echo.
echo This will remove:
echo - All installed versions
echo - Registry entries
echo - Application data
echo - Shortcuts
echo.
pause

echo.
echo Step 1: Uninstalling from Programs...
echo.

REM Uninstall using Windows installer
wmic product where "name like '%%2048%%'" call uninstall /nointeractive 2>nul
wmic product where "name like '%%Hexa%%'" call uninstall /nointeractive 2>nul

echo.
echo Step 2: Removing installation directories...
echo.

REM Remove Program Files directories
if exist "C:\Program Files\2048HexaGame\" (
    echo Removing: C:\Program Files\2048HexaGame\
    rmdir /S /Q "C:\Program Files\2048HexaGame\"
)

if exist "C:\Program Files (x86)\2048HexaGame\" (
    echo Removing: C:\Program Files (x86)\2048HexaGame\
    rmdir /S /Q "C:\Program Files (x86)\2048HexaGame\"
)

echo.
echo Step 3: Removing application data...
echo.

REM Remove app data
if exist "%LOCALAPPDATA%\hexabrain\" (
    echo Removing: %LOCALAPPDATA%\hexabrain\
    rmdir /S /Q "%LOCALAPPDATA%\hexabrain\"
)

if exist "%LOCALAPPDATA%\2048HexaGame\" (
    echo Removing: %LOCALAPPDATA%\2048HexaGame\
    rmdir /S /Q "%LOCALAPPDATA%\2048HexaGame\"
)

echo.
echo Step 4: Removing shortcuts...
echo.

del "%USERPROFILE%\Desktop\2048*.lnk" 2>nul
del "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Hexa Games\*.lnk" 2>nul
rmdir "%APPDATA%\Microsoft\Windows\Start Menu\Programs\Hexa Games" 2>nul

echo.
echo Step 5: Cleaning registry (requires admin)...
echo.

REM Clean registry entries (silent, ignore errors)
reg delete "HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame" /f 2>nul
reg delete "HKCU\SOFTWARE\2048HexaGame" /f 2>nul
reg delete "HKLM\SOFTWARE\2048HexaGame" /f 2>nul

echo.
echo ========================================
echo  CLEANUP COMPLETE!
echo ========================================
echo.
echo All traces removed. Now install the new version.
echo.
pause
