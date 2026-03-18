Write-Host "========================================" -ForegroundColor Green
Write-Host "  Creating Portable App (No Installer)" -ForegroundColor Green
Write-Host "  Complete Runtime + Ready to Run" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

$ErrorActionPreference = "Stop"

$projectRoot = "D:\Hexabrain\Other\compose-multiplatform-2048"
$outputDir = "$projectRoot\build\portable-app"
$runtimeDir = "$projectRoot\build\final-runtime"
$jdk = "C:\Program Files\Java\jdk-17"

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

# Reuse existing runtime if it exists
if (!(Test-Path $runtimeDir)) {
    Write-Host "Step 2: Creating complete Java runtime..." -ForegroundColor Yellow

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
} else {
    Write-Host "Step 2: Using existing runtime..." -ForegroundColor Yellow
    $runtimeSize = (Get-ChildItem $runtimeDir -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
    Write-Host "Runtime size: $([math]::Round($runtimeSize, 2)) MB" -ForegroundColor Green
    Write-Host ""
}

Write-Host "Step 3: Creating app-image (portable app)..." -ForegroundColor Yellow

# Clean output directory
if (Test-Path $outputDir) {
    Remove-Item -Recurse -Force $outputDir
}
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$iconFile = "$projectRoot\composeApp\src\desktopMain\resources\icons\app-icon.ico"
if (!(Test-Path $iconFile)) {
    Write-Host "ERROR: Icon file not found!" -ForegroundColor Red
    exit 1
}

$jpackageArgs = @(
    "--type", "app-image",
    "--name", "2048HexaGame",
    "--app-version", "1.0.1",
    "--vendor", "Hexabrain Systems",
    "--description", "2048 Hexa Game - Slide to combine numbers and reach 2048!",
    "--copyright", "Copyright 2026 Hexabrain Systems",
    "--input", "$projectRoot\composeApp\build\libs",
    "--main-jar", "composeApp-desktop.jar",
    "--main-class", "com.alexjlockwood.twentyfortyeight.MainKt",
    "--runtime-image", $runtimeDir,
    "--icon", $iconFile,
    "--dest", $outputDir,
    "--java-options", "-Xms128m",
    "--java-options", "-Xmx1024m",
    "--java-options", "-Dsun.java2d.d3d=false",
    "--java-options", "-Dfile.encoding=UTF-8",
    "--verbose"
)

& "$jdk\bin\jpackage.exe" @jpackageArgs

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: jpackage failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  SUCCESS! Portable App Created!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

$appDir = Get-ChildItem "$outputDir\*" -Directory | Select-Object -First 1
if ($appDir) {
    $appSize = (Get-ChildItem $appDir.FullName -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
    Write-Host "App Directory: $($appDir.Name)" -ForegroundColor Yellow
    Write-Host "Size: $([math]::Round($appSize, 2)) MB" -ForegroundColor Yellow
    Write-Host "Location: $($appDir.FullName)" -ForegroundColor Cyan
    Write-Host ""

    # Copy to project root
    $finalAppDir = "$projectRoot\2048-Hexa-Game-Portable"
    if (Test-Path $finalAppDir) {
        Remove-Item -Recurse -Force $finalAppDir
    }
    Copy-Item -Recurse $appDir.FullName $finalAppDir
    Write-Host "Copied to: $finalAppDir" -ForegroundColor Green
    Write-Host ""

    # Find the exe
    $exeFile = Get-ChildItem "$finalAppDir\*.exe" -Recurse | Select-Object -First 1
    if ($exeFile) {
        Write-Host "Run the app: $($exeFile.FullName)" -ForegroundColor Cyan
        Write-Host ""
    }
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  USAGE INSTRUCTIONS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This is a PORTABLE app (no installation needed!)" -ForegroundColor White
Write-Host ""
Write-Host "1. Navigate to: $finalAppDir" -ForegroundColor White
Write-Host "2. Run: 2048HexaGame.exe" -ForegroundColor White
Write-Host "3. App will launch WITHOUT JVM error!" -ForegroundColor Green
Write-Host ""
Write-Host "You can copy this entire folder anywhere and it will work!" -ForegroundColor Yellow
Write-Host ""
