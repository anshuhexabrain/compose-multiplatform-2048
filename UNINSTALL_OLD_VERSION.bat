@echo off
echo.
echo ========================================
echo   Uninstall Old Version
echo ========================================
echo.
echo Opening Windows Settings to uninstall...
echo.
echo Steps:
echo 1. Find "com.hexa.game.twentyfourtyeight" in the apps list
echo 2. Click on it and select "Uninstall"
echo 3. Wait for uninstallation to complete
echo 4. Close this window when done
echo.
echo Opening Settings in 3 seconds...
timeout /t 3 /nobreak > nul

start ms-settings:appsfeatures

echo.
echo Settings opened!
echo After uninstalling, run the new installer.
echo.
pause
