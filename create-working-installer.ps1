Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Creating WORKING 2048 Hexa Game - Merge, Match, Master the Puzzle Installer" -ForegroundColor Cyan
Write-Host "  With ALL Dependencies & Runtime" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"

$projectRoot = "D:\Hexabrain\Other\compose-multiplatform-2048"
$outputDir = "$projectRoot\build\working-installer"
$runtimeDir = "$projectRoot\build\custom-runtime"
$jdk = "C:\Program Files\Java\jdk-17"
$wixPath = "$projectRoot\build\wix311"

# Add WiX to PATH
$env:PATH = "$wixPath;$env:PATH"

Write-Host "Step 1: Building application JAR with Gradle..." -ForegroundColor Yellow
Set-Location $projectRoot
& .\gradlew.bat :composeApp:desktopJar --no-daemon
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to build JAR" -ForegroundColor Red
    exit 1
}
Write-Host "JAR built successfully!" -ForegroundColor Green
Write-Host ""

# Check JAR
$jarFile = "$projectRoot\composeApp\build\libs\composeApp-desktop.jar"
if (!(Test-Path $jarFile)) {
    Write-Host "ERROR: JAR not found at $jarFile" -ForegroundColor Red
    exit 1
}
Write-Host "JAR Location: $jarFile" -ForegroundColor Cyan
Write-Host ""

Write-Host "Step 2: Creating Java runtime with ALL required modules..." -ForegroundColor Yellow

# Remove old runtime
if (Test-Path $runtimeDir) {
    Remove-Item -Recurse -Force $runtimeDir
}

# Include ALL modules that Compose Desktop needs
$modules = "java.base,java.desktop,java.datatransfer,java.logging,java.xml,java.naming,java.sql,java.prefs,java.management,java.scripting,jdk.unsupported,jdk.crypto.ec,jdk.accessibility,java.compiler,jdk.zipfs"

Write-Host "Modules: $modules" -ForegroundColor Cyan

& "$jdk\bin\jlink.exe" `
  --add-modules $modules `
  --output $runtimeDir `
  --strip-debug `
  --no-header-files `
  --no-man-pages `
  --compress=2

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Failed to create runtime" -ForegroundColor Red
    exit 1
}
Write-Host "Runtime created!" -ForegroundColor Green
Write-Host ""

# Create output directory
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

# Check for ICO icon
$iconFile = "$projectRoot\composeApp\src\desktopMain\resources\icons\app-icon.ico"
if (!(Test-Path $iconFile)) {
    Write-Host "WARNING: ICO icon not found, will use default" -ForegroundColor Yellow
    $iconFile = ""
}

Write-Host "Step 3: Creating EXE installer with jpackage..." -ForegroundColor Yellow
Write-Host ""

$jpackageArgs = @(
    "--type", "exe",
    "--name", "2048 Hexa Game - Merge, Match, Master the Puzzle",
    "--app-version", "1.0.0",
    "--vendor", "Hexabrain Systems",
    "--description", "2048 Hexa Game - Merge, Match, Master the Puzzle",
    "--copyright", "Copyright 2026 Hexabrain Systems",
    "--input", "$projectRoot\composeApp\build\libs",
    "--main-jar", "composeApp-desktop.jar",
    "--main-class", "com.alexjlockwood.twentyfortyeight.MainKt",
    "--runtime-image", $runtimeDir,
    "--win-menu",
    "--win-shortcut",
    "--win-menu-group", "Hexa Games",
    "--dest", $outputDir,
    "--java-options", "-Xmx512m",
    "--java-options", "-Dsun.java2d.d3d=false",
    "--verbose"
)

if ($iconFile -ne "") {
    $jpackageArgs += "--icon"
    $jpackageArgs += $iconFile
    Write-Host "Using icon: $iconFile" -ForegroundColor Cyan
}

& "$jdk\bin\jpackage.exe" @jpackageArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: jpackage failed" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  SUCCESS! Installer Created!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Output: $outputDir" -ForegroundColor Cyan
Write-Host ""

$installer = Get-ChildItem "$outputDir\*.exe" | Select-Object -First 1
if ($installer) {
    $size = [math]::Round($installer.Length/1MB, 2)
    Write-Host "Installer: $($installer.Name)" -ForegroundColor Yellow
    Write-Host "Size: $size MB" -ForegroundColor Yellow
    Write-Host ""

    # Copy to project root
    $finalInstaller = "$projectRoot\2048-Hexa-Game-WORKING.exe"
    Copy-Item $installer.FullName $finalInstaller -Force
    Write-Host "Copied to: $finalInstaller" -ForegroundColor Green
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INSTALLATION INSTRUCTIONS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Uninstall old version from Control Panel" -ForegroundColor White
Write-Host "2. Run: $finalInstaller" -ForegroundColor White
Write-Host "3. Launch app from desktop shortcut" -ForegroundColor White
Write-Host "4. Should work WITHOUT JVM error!" -ForegroundColor Green
Write-Host ""



