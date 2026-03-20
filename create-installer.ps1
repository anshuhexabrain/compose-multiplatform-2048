Write-Host "========================================"
Write-Host "  Building 2048 Hexa Game - Merge, Match, Master the Puzzle Installer"
Write-Host "========================================"
Write-Host ""

$ErrorActionPreference = "Stop"

# Set paths
$projectRoot = "D:\Hexabrain\Other\compose-multiplatform-2048"
$jarFile = "$projectRoot\composeApp\build\libs\composeApp-desktop.jar"
$iconFile = "$projectRoot\composeApp\src\desktopMain\resources\icons\app-icon.png"
$outputDir = "$projectRoot\build\installers"
$jpackage = "C:\Program Files\Java\jdk-17\bin\jpackage.exe"

# Check if JAR exists
if (!(Test-Path $jarFile)) {
    Write-Host "ERROR: JAR file not found at $jarFile" -ForegroundColor Red
    Write-Host "Building JAR first..." -ForegroundColor Yellow
    Set-Location $projectRoot
    & .\gradlew.bat :composeApp:desktopJar
}

# Create output directory
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

Write-Host "Creating EXE installer..." -ForegroundColor Green

# Create EXE installer
& $jpackage `
  --type exe `
  --name "2048 Hexa Game - Merge, Match, Master the Puzzle" `
  --app-version "1.0.0" `
  --vendor "Hexabrain Systems" `
  --description "2048 Hexa Game - Merge, Match, Master the Puzzle" `
  --copyright "Copyright 2026 Hexabrain Systems" `
  --icon $iconFile `
  --input "$projectRoot\composeApp\build\libs" `
  --main-jar "composeApp-desktop.jar" `
  --win-menu `
  --win-shortcut `
  --win-menu-group "Hexa Games" `
  --dest $outputDir

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "SUCCESS! Installer created:" -ForegroundColor Green
    Get-ChildItem $outputDir\*.exe | ForEach-Object {
        Write-Host "  $($_.Name) - $([math]::Round($_.Length/1MB, 2)) MB" -ForegroundColor Cyan
    }
} else {
    Write-Host "ERROR: Failed to create installer" -ForegroundColor Red
}

Write-Host ""
Write-Host "Output directory: $outputDir"
Write-Host ""



