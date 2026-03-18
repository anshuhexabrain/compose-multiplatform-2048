# 🎨 How to Add Windows Icon (.ico) for Better Installer Appearance

## Current Status
✅ PNG icon already configured: `composeApp/src/desktopMain/resources/icons/app-icon.png`
✅ Build configuration updated with icon path
✅ App name changed to "2048 Hexa Game"

## 🔵 Optional: Convert to Windows .ico for Perfect Quality

For the best-looking Windows installer and app icon, convert the PNG to .ico format:

### Method 1: Online Converter (Easiest)
1. Go to: https://convertio.co/png-ico/ or https://www.icoconverter.com/
2. Upload: `composeApp/src/desktopMain/resources/icons/app-icon.png`
3. Select ICO format with multiple sizes: 16x16, 32x32, 48x48, 256x256
4. Download the .ico file
5. Save it as: `composeApp/src/desktopMain/resources/icons/app-icon.ico`

### Method 2: Using PowerShell (Windows Built-in)
Run this PowerShell script from project root:

```powershell
# Install required module
Install-Module -Name ImageConverter -Force

# Convert PNG to ICO
$pngPath = "composeApp/src/desktopMain/resources/icons/app-icon.png"
$icoPath = "composeApp/src/desktopMain/resources/icons/app-icon.ico"

Add-Type -AssemblyName System.Drawing
$img = [System.Drawing.Image]::FromFile((Resolve-Path $pngPath))
$icon = [System.Drawing.Icon]::FromHandle($img.GetHicon())
$stream = [System.IO.File]::Create($icoPath)
$icon.Save($stream)
$stream.Close()

Write-Host "✓ Icon created: $icoPath"
```

### Method 3: Using ImageMagick (If installed)
```bash
# Convert PNG to multi-size ICO
magick convert app-icon.png -define icon:auto-resize=256,128,96,64,48,32,16 app-icon.ico
```

## 📦 What Happens When You Build

### With PNG only (Current):
- ✅ Gradle will auto-convert PNG to ICO
- ✅ Icon will appear in installer
- ✅ Icon will appear on desktop shortcut
- ⚠️ Quality may be slightly lower

### With ICO file (Recommended):
- ✅ Best quality icon
- ✅ Multiple sizes included (16x16 to 256x256)
- ✅ Perfect rendering at all sizes
- ✅ Professional appearance

## 🚀 Build Your Installer

After adding the icon (PNG is already configured, ICO is optional):

```bash
cd D:\Hexabrain\Other\compose-multiplatform-2048

# Clean build
gradlew clean

# Create Windows installer
gradlew :composeApp:packageDistributionForCurrentOS

# Find your installer:
# - EXE: composeApp/build/compose/binaries/main/exe/2048HexaGame-1.0.0.exe
# - MSI: composeApp/build/compose/binaries/main/msi/2048HexaGame-1.0.0.msi
```

## ✨ Your Installer Will Now Show:

✅ **App Name:** 2048 Hexa Game
✅ **Icon:** Your game logo
✅ **Menu Group:** Hexa Games
✅ **Desktop Shortcut:** With icon
✅ **Publisher:** Hexabrain Systems
✅ **Description:** Slide to combine numbers and reach 2048!

## 🎯 Preview

When users install your app, they'll see:
- Professional icon in Windows Programs list
- Icon on desktop shortcut
- Icon in taskbar when running
- Icon in Windows uninstaller
- Icon in installer wizard

---

**Current icon location:**
```
composeApp/src/desktopMain/resources/icons/app-icon.png ✓
```

**You're ready to build!** The PNG icon will work perfectly. Converting to ICO is optional for slightly better quality.
