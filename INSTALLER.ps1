Write-Host "========================================" -ForegroundColor Green
Write-Host "  2048 Hexa Game - Installer" -ForegroundColor Green
Write-Host "  Version 1.0.1" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Check for admin rights
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "⚠ WARNING: Running without administrator privileges" -ForegroundColor Yellow
    Write-Host "Some features may not work properly. Consider right-clicking and 'Run as Administrator'" -ForegroundColor Yellow
    Write-Host ""
    $continue = Read-Host "Continue anyway? (Y/N)"
    if ($continue -ne "Y" -and $continue -ne "y") {
        exit
    }
}

$portableAppDir = "$PSScriptRoot\2048-Hexa-Game-Portable"
$installDir = "$env:ProgramFiles\2048HexaGame"

# Check if portable app exists
if (!(Test-Path $portableAppDir)) {
    Write-Host "ERROR: Portable app directory not found!" -ForegroundColor Red
    Write-Host "Expected: $portableAppDir" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please run create-portable-app.ps1 first!" -ForegroundColor Yellow
    pause
    exit 1
}

Write-Host "Installation Options:" -ForegroundColor Cyan
Write-Host "1. Install for All Users (requires admin)" -ForegroundColor White
Write-Host "2. Install for Current User only" -ForegroundColor White
Write-Host ""
$choice = Read-Host "Choose (1 or 2)"

if ($choice -eq "2") {
    $installDir = "$env:LOCALAPPDATA\Programs\2048HexaGame"
    $programMenu = "$env:APPDATA\Microsoft\Windows\Start Menu\Programs"
    $regPath = "HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame"
} else {
    $installDir = "$env:ProgramFiles\2048HexaGame"
    $programMenu = "$env:ProgramData\Microsoft\Windows\Start Menu\Programs"
    $regPath = "HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame"
}

Write-Host ""
Write-Host "Installing to: $installDir" -ForegroundColor Cyan
Write-Host ""

# Remove old installation if exists
if (Test-Path $installDir) {
    Write-Host "Removing old installation..." -ForegroundColor Yellow
    try {
        Remove-Item -Recurse -Force $installDir -ErrorAction Stop
    } catch {
        Write-Host "ERROR: Cannot remove old installation. Please uninstall manually first." -ForegroundColor Red
        pause
        exit 1
    }
}

# Create install directory
Write-Host "Copying files..." -ForegroundColor Yellow
New-Item -ItemType Directory -Force -Path $installDir | Out-Null

# Copy all files
Copy-Item -Recurse "$portableAppDir\*" $installDir -Force

Write-Host "Creating shortcuts..." -ForegroundColor Yellow

# Create Start Menu folder
$startMenuFolder = "$programMenu\Hexa Games"
New-Item -ItemType Directory -Force -Path $startMenuFolder | Out-Null

# Create shortcuts
$WshShell = New-Object -ComObject WScript.Shell

# Desktop shortcut
$desktopShortcut = $WshShell.CreateShortcut("$env:USERPROFILE\Desktop\2048 Hexa Game.lnk")
$desktopShortcut.TargetPath = "$installDir\2048HexaGame.exe"
$desktopShortcut.IconLocation = "$installDir\2048HexaGame.ico"
$desktopShortcut.WorkingDirectory = $installDir
$desktopShortcut.Description = "2048 Hexa Game - Slide to combine numbers!"
$desktopShortcut.Save()

# Start Menu shortcut
$startMenuShortcut = $WshShell.CreateShortcut("$startMenuFolder\2048 Hexa Game.lnk")
$startMenuShortcut.TargetPath = "$installDir\2048HexaGame.exe"
$startMenuShortcut.IconLocation = "$installDir\2048HexaGame.ico"
$startMenuShortcut.WorkingDirectory = $installDir
$startMenuShortcut.Description = "2048 Hexa Game - Slide to combine numbers!"
$startMenuShortcut.Save()

Write-Host "Adding to Programs & Features..." -ForegroundColor Yellow

# Create uninstaller script
$uninstallerContent = @"
Write-Host "Uninstalling 2048 Hexa Game..." -ForegroundColor Yellow
Remove-Item -Recurse -Force "$installDir" -ErrorAction SilentlyContinue
Remove-Item -Force "$env:USERPROFILE\Desktop\2048 Hexa Game.lnk" -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force "$startMenuFolder" -ErrorAction SilentlyContinue
Remove-Item -Path "$regPath" -Recurse -Force -ErrorAction SilentlyContinue
Write-Host "Uninstallation complete!" -ForegroundColor Green
pause
"@

$uninstallerContent | Out-File -FilePath "$installDir\Uninstall.ps1" -Encoding UTF8

# Registry entry for Add/Remove Programs
if ($choice -ne "2" -or $isAdmin) {
    try {
        New-Item -Path $regPath -Force | Out-Null
        Set-ItemProperty -Path $regPath -Name "DisplayName" -Value "2048 Hexa Game"
        Set-ItemProperty -Path $regPath -Name "DisplayVersion" -Value "1.0.1"
        Set-ItemProperty -Path $regPath -Name "Publisher" -Value "Hexabrain Systems"
        Set-ItemProperty -Path $regPath -Name "InstallLocation" -Value $installDir
        Set-ItemProperty -Path $regPath -Name "UninstallString" -Value "powershell.exe -ExecutionPolicy Bypass -File `"$installDir\Uninstall.ps1`""
        Set-ItemProperty -Path $regPath -Name "DisplayIcon" -Value "$installDir\2048HexaGame.ico"
        Set-ItemProperty -Path $regPath -Name "NoModify" -Value 1 -Type DWord
        Set-ItemProperty -Path $regPath -Name "NoRepair" -Value 1 -Type DWord
    } catch {
        Write-Host "Warning: Could not add to Programs & Features (insufficient permissions)" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Installation Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "App installed to: $installDir" -ForegroundColor Cyan
Write-Host "Shortcuts created on Desktop and Start Menu" -ForegroundColor Cyan
Write-Host ""
Write-Host "Launch the app from:" -ForegroundColor Yellow
Write-Host "  - Desktop shortcut: '2048 Hexa Game'" -ForegroundColor White
Write-Host "  - Start Menu: Hexa Games > 2048 Hexa Game" -ForegroundColor White
Write-Host ""
$launch = Read-Host "Launch now? (Y/N)"
if ($launch -eq "Y" -or $launch -eq "y") {
    Start-Process "$installDir\2048HexaGame.exe"
}
