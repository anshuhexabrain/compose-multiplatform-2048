Write-Host "========================================"
Write-Host "  Building 2048 Hexa Game Installers"
Write-Host "========================================"
Write-Host ""

$ErrorActionPreference = "Stop"

# Set paths
$projectRoot = "D:\Hexabrain\Other\compose-multiplatform-2048"
$jarFile = "$projectRoot\composeApp\build\libs\composeApp-desktop.jar"
$iconFile = "$projectRoot\composeApp\src\desktopMain\resources\icons\app-icon.png"
$outputDir = "$projectRoot\build\installers"
$jpackage = "C:\Program Files\Java\jdk-17\bin\jpackage.exe"
$wixPath = "$projectRoot\build\wix311"

# Add WiX to PATH
$env:PATH = "$wixPath;$env:PATH"
Write-Host "Added WiX to PATH: $wixPath" -ForegroundColor Yellow

# Check if JAR exists
if (!(Test-Path $jarFile)) {
    Write-Host "ERROR: JAR file not found at $jarFile" -ForegroundColor Red
    exit 1
}

# Create output directory
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

Write-Host ""
Write-Host "Creating EXE installer..." -ForegroundColor Green
Write-Host ""

# Create EXE installer
& $jpackage `
  --type exe `
  --name "2048HexaGame" `
  --app-version "1.0.0" `
  --vendor "Hexabrain Systems" `
  --description "2048 Hexa Game - Slide to combine numbers and reach 2048!" `
  --copyright "Copyright 2026 Hexabrain Systems" `
  --icon $iconFile `
  --input "$projectRoot\composeApp\build\libs" `
  --main-jar "composeApp-desktop.jar" `
  --main-class "com.alexjlockwood.twentyfortyeight.MainKt" `
  --win-menu `
  --win-shortcut `
  --win-menu-group "Hexa Games" `
  --dest $outputDir

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "SUCCESS! EXE Installer created!" -ForegroundColor Green
    Write-Host ""
}

Write-Host "Creating MSI installer..." -ForegroundColor Green
Write-Host ""

# Create MSI installer
& $jpackage `
  --type msi `
  --name "2048HexaGame" `
  --app-version "1.0.0" `
  --vendor "Hexabrain Systems" `
  --description "2048 Hexa Game - Slide to combine numbers and reach 2048!" `
  --copyright "Copyright 2026 Hexabrain Systems" `
  --icon $iconFile `
  --input "$projectRoot\composeApp\build\libs" `
  --main-jar "composeApp-desktop.jar" `
  --main-class "com.alexjlockwood.twentyfortyeight.MainKt" `
  --win-menu `
  --win-menu-group "Hexa Games" `
  --dest $outputDir

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "SUCCESS! MSI Installer created!" -ForegroundColor Green
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  INSTALLERS CREATED SUCCESSFULLY!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Location: $outputDir" -ForegroundColor Cyan
Write-Host ""
Get-ChildItem $outputDir | ForEach-Object {
    $size = [math]::Round($_.Length/1MB, 2)
    Write-Host "  $($_.Name) - $size MB" -ForegroundColor Yellow
}
Write-Host ""
