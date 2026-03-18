Write-Host "========================================"
Write-Host "  Building 2048 Hexa Game Installer"
Write-Host "  With Bundled Java Runtime"
Write-Host "========================================"
Write-Host ""

$ErrorActionPreference = "Stop"

# Set paths
$projectRoot = "D:\Hexabrain\Other\compose-multiplatform-2048"
$jarFile = "$projectRoot\composeApp\build\libs\composeApp-desktop.jar"
$iconFile = "$projectRoot\composeApp\src\desktopMain\resources\icons\app-icon.png"
$outputDir = "$projectRoot\build\installers"
$runtimeDir = "$projectRoot\build\runtime-image"
$jdk = "C:\Program Files\Java\jdk-17"
$jpackage = "$jdk\bin\jpackage.exe"
$jlink = "$jdk\bin\jlink.exe"
$wixPath = "$projectRoot\build\wix311"

# Add WiX to PATH
$env:PATH = "$wixPath;$env:PATH"

# Create output directory
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

Write-Host "Step 1: Analyzing JAR for required modules..." -ForegroundColor Yellow
$modules = & "$jdk\bin\jdeps" --print-module-deps --ignore-missing-deps $jarFile 2>&1
if ($modules -match "Error") {
    Write-Host "Warning: Could not analyze modules, using default set" -ForegroundColor Yellow
    $modules = "java.base,java.desktop,java.logging,java.xml,java.naming,java.sql,jdk.unsupported"
} else {
    # Add additional required modules for Compose Desktop
    $modules = "$modules,java.desktop,java.sql,jdk.unsupported,jdk.crypto.ec"
}
Write-Host "Required modules: $modules" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 2: Creating custom Java runtime with jlink..." -ForegroundColor Yellow
if (Test-Path $runtimeDir) {
    Remove-Item -Recurse -Force $runtimeDir
}

& $jlink `
  --add-modules $modules `
  --output $runtimeDir `
  --strip-debug `
  --no-header-files `
  --no-man-pages `
  --compress=2

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to create runtime image" -ForegroundColor Red
    exit 1
}
Write-Host "Runtime created successfully!" -ForegroundColor Green
Write-Host ""

Write-Host "Step 3: Creating EXE installer with bundled runtime..." -ForegroundColor Yellow
Write-Host ""

# Create EXE installer with runtime
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
  --runtime-image $runtimeDir `
  --win-menu `
  --win-shortcut `
  --win-menu-group "Hexa Games" `
  --dest $outputDir `
  --verbose

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "SUCCESS! EXE Installer created!" -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host "ERROR: Failed to create EXE installer" -ForegroundColor Red
    exit 1
}

Write-Host "Step 4: Creating MSI installer with bundled runtime..." -ForegroundColor Yellow
Write-Host ""

# Create MSI installer with runtime
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
  --runtime-image $runtimeDir `
  --win-menu `
  --win-menu-group "Hexa Games" `
  --dest $outputDir `
  --verbose

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
Get-ChildItem $outputDir -Filter "*.exe","*.msi" | ForEach-Object {
    $size = [math]::Round($_.Length/1MB, 2)
    Write-Host "  $($_.Name) - $size MB" -ForegroundColor Yellow
}
Write-Host ""
Write-Host "These installers include a bundled Java runtime" -ForegroundColor Green
Write-Host "Users do NOT need Java installed!" -ForegroundColor Green
Write-Host ""
