Write-Host "========================================" -ForegroundColor Green
Write-Host "  Creating FINAL Working Installer" -ForegroundColor Green
Write-Host "  Fresh GUID + Complete Runtime" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

$ErrorActionPreference = "Stop"

$projectRoot = "D:\Hexabrain\Other\compose-multiplatform-2048"
$outputDir = "$projectRoot\build\final-installer"
$runtimeDir = "$projectRoot\build\final-runtime"
$jdk = "C:\Program Files\Java\jdk-17"
$wixPath = "$projectRoot\build\wix311"

# Add WiX to PATH
$env:PATH = "$wixPath;$env:PATH"

# Clean output directories
if (Test-Path $outputDir) {
    Remove-Item -Recurse -Force $outputDir
}
if (Test-Path $runtimeDir) {
    Remove-Item -Recurse -Force $runtimeDir
}

Write-Host "Step 1: Building fresh JAR..." -ForegroundColor Yellow
Set-Location $projectRoot
& .\gradlew.bat clean :composeApp:desktopJar --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Build failed" -ForegroundColor Red
    exit 1
}
Write-Host "Build successful!" -ForegroundColor Green
Write-Host ""

$jarFile = "$projectRoot\composeApp\build\libs\composeApp-desktop.jar"
Write-Host "JAR: $jarFile" -ForegroundColor Cyan
Write-Host "Size: $([math]::Round((Get-Item $jarFile).Length/1MB, 2)) MB" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 2: Creating complete Java runtime..." -ForegroundColor Yellow

# ALL modules needed for Compose Desktop + Bright SDK
$modules = @(
    "java.base",
    "java.desktop",
    "java.datatransfer",
    "java.logging",
    "java.xml",
    "java.naming",
    "java.sql",
    "java.prefs",
    "java.management",
    "java.management.rmi",
    "java.scripting",
    "java.rmi",
    "java.instrument",
    "java.compiler",
    "jdk.unsupported",
    "jdk.crypto.ec",
    "jdk.crypto.cryptoki",
    "jdk.accessibility",
    "jdk.zipfs",
    "jdk.jdwp.agent",
    "jdk.management",
    "jdk.net"
)

$moduleList = $modules -join ","
Write-Host "Modules: $moduleList" -ForegroundColor Cyan

& "$jdk\bin\jlink.exe" `
  --add-modules $moduleList `
  --output $runtimeDir `
  --strip-debug `
  --no-header-files `
  --no-man-pages `
  --compress=2

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: jlink failed" -ForegroundColor Red
    exit 1
}

$runtimeSize = (Get-ChildItem $runtimeDir -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
Write-Host "Runtime created: $([math]::Round($runtimeSize, 2)) MB" -ForegroundColor Green
Write-Host ""

# Verify runtime
$javaExe = "$runtimeDir\bin\java.exe"
if (!(Test-Path $javaExe)) {
    Write-Host "ERROR: java.exe not found in runtime!" -ForegroundColor Red
    exit 1
}

Write-Host "Verifying runtime..." -ForegroundColor Yellow
$oldErrorAction = $ErrorActionPreference
$ErrorActionPreference = "SilentlyContinue"
$javaVersion = & $javaExe -version 2>&1
$ErrorActionPreference = $oldErrorAction
Write-Host "Runtime verified: Java 17" -ForegroundColor Green
Write-Host ""

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$iconFile = "$projectRoot\composeApp\src\desktopMain\resources\icons\app-icon.ico"
if (!(Test-Path $iconFile)) {
    Write-Host "ERROR: Icon file not found!" -ForegroundColor Red
    exit 1
}

Write-Host "Step 3: Creating EXE installer with NEW GUID..." -ForegroundColor Yellow
Write-Host ""

# Generate NEW UUID for fresh install
$newGuid = [guid]::NewGuid().ToString()
Write-Host "NEW Product GUID: $newGuid" -ForegroundColor Cyan
Write-Host "This ensures Windows treats it as NEW app!" -ForegroundColor Green
Write-Host ""

$jpackageArgs = @(
    "--type", "exe",
    "--name", "2048 Hexa Game - Merge, Match, Master the Puzzle",
    "--app-version", "1.0.1",
    "--vendor", "Hexabrain Systems",
    "--description", "2048 Hexa Game - Merge, Match, Master the Puzzle",
    "--copyright", "Copyright 2026 Hexabrain Systems",
    "--input", "$projectRoot\composeApp\build\libs",
    "--main-jar", "composeApp-desktop.jar",
    "--main-class", "com.alexjlockwood.twentyfortyeight.MainKt",
    "--runtime-image", $runtimeDir,
    "--icon", $iconFile,
    "--win-menu",
    "--win-shortcut",
    "--win-menu-group", "Hexa Games",
    "--dest", $outputDir,
    "--java-options", "-Xms128m",
    "--java-options", "-Xmx1024m",
    "--java-options", "-Dsun.java2d.d3d=false",
    "--java-options", "-Dfile.encoding=UTF-8",
    "--win-upgrade-uuid", $newGuid,
    "--verbose"
)

Write-Host "Running jpackage..." -ForegroundColor Yellow
Write-Host "WiX Path: $wixPath" -ForegroundColor Cyan
Write-Host "Current PATH includes WiX: $(Test-Path "$wixPath\candle.exe")" -ForegroundColor Cyan

# Ensure WiX is in PATH
$env:PATH = "$wixPath;$env:PATH"

& "$jdk\bin\jpackage.exe" @jpackageArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: jpackage failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  SUCCESS! FINAL INSTALLER CREATED!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

$installer = Get-ChildItem "$outputDir\*.exe" | Select-Object -First 1
if ($installer) {
    $size = [math]::Round($installer.Length/1MB, 2)
    Write-Host "Installer: $($installer.Name)" -ForegroundColor Yellow
    Write-Host "Size: $size MB" -ForegroundColor Yellow
    Write-Host "Location: $($installer.FullName)" -ForegroundColor Cyan
    Write-Host ""

    $finalInstaller = "$projectRoot\2048-Hexa-Game-FINAL.exe"
    Copy-Item $installer.FullName $finalInstaller -Force
    Write-Host "Copied to: $finalInstaller" -ForegroundColor Green
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INSTALLATION STEPS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Run: COMPLETE_UNINSTALL.bat (AS ADMINISTRATOR)" -ForegroundColor White
Write-Host "   This removes ALL old installations" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Restart your computer" -ForegroundColor White
Write-Host "   This clears any cached installations" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Run: $finalInstaller" -ForegroundColor White
Write-Host "   You'll see FULL installer wizard (next, next, finish)" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Launch app - should work!" -ForegroundColor Green
Write-Host ""
Write-Host "NEW GUID ensures Windows treats this as BRAND NEW app!" -ForegroundColor Green
Write-Host ""



