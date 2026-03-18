Add-Type -AssemblyName System.Drawing

$pngPath = "D:\Hexabrain\Other\compose-multiplatform-2048\composeApp\src\desktopMain\resources\icons\app-icon.png"
$icoPath = "D:\Hexabrain\Other\compose-multiplatform-2048\composeApp\src\desktopMain\resources\icons\app-icon.ico"

Write-Host "Converting PNG to ICO..." -ForegroundColor Yellow

# Load the PNG image
$img = [System.Drawing.Image]::FromFile($pngPath)

# Create icon with multiple sizes
$icon = [System.Drawing.Icon]::FromHandle($img.GetHicon())

# Save as ICO
$stream = [System.IO.File]::Create($icoPath)
$icon.Save($stream)
$stream.Close()

$img.Dispose()

Write-Host "Icon converted successfully!" -ForegroundColor Green
Write-Host "Saved to: $icoPath" -ForegroundColor Cyan
