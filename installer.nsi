; 2048 Hexa Game Installer Script for NSIS

!include "MUI2.nsh"

; App Details
Name "2048 Hexa Game"
OutFile "2048-Hexa-Game-Setup.exe"
InstallDir "$PROGRAMFILES\2048HexaGame"
RequestExecutionLevel admin

; Version Info
VIProductVersion "1.0.1.0"
VIAddVersionKey "ProductName" "2048 Hexa Game"
VIAddVersionKey "CompanyName" "Hexabrain Systems"
VIAddVersionKey "FileDescription" "2048 Hexa Game Installer"
VIAddVersionKey "FileVersion" "1.0.1"
VIAddVersionKey "ProductVersion" "1.0.1"
VIAddVersionKey "LegalCopyright" "Copyright 2026 Hexabrain Systems"

; Modern UI Configuration
!define MUI_ICON "composeApp\src\desktopMain\resources\icons\app-icon.ico"
!define MUI_HEADERIMAGE
!define MUI_ABORTWARNING
!define MUI_FINISHPAGE_RUN "$INSTDIR\2048HexaGame.exe"
!define MUI_FINISHPAGE_RUN_TEXT "Launch 2048 Hexa Game"

; Pages
!insertmacro MUI_PAGE_LICENSE "LICENSE.txt"
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

; Language
!insertmacro MUI_LANGUAGE "English"

; Installer Section
Section "Install"
    SetOutPath "$INSTDIR"

    ; Copy all files from portable app
    File /r "2048-Hexa-Game-Portable\*.*"

    ; Create shortcuts
    CreateDirectory "$SMPROGRAMS\Hexa Games"
    CreateShortcut "$SMPROGRAMS\Hexa Games\2048 Hexa Game.lnk" "$INSTDIR\2048HexaGame.exe" "" "$INSTDIR\2048HexaGame.ico"
    CreateShortcut "$DESKTOP\2048 Hexa Game.lnk" "$INSTDIR\2048HexaGame.exe" "" "$INSTDIR\2048HexaGame.ico"

    ; Write uninstaller
    WriteUninstaller "$INSTDIR\Uninstall.exe"

    ; Registry for Add/Remove Programs
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame" "DisplayName" "2048 Hexa Game"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame" "UninstallString" "$INSTDIR\Uninstall.exe"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame" "DisplayIcon" "$INSTDIR\2048HexaGame.ico"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame" "Publisher" "Hexabrain Systems"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame" "DisplayVersion" "1.0.1"
    WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame" "NoModify" 1
    WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame" "NoRepair" 1
SectionEnd

; Uninstaller Section
Section "Uninstall"
    ; Remove files and directories
    RMDir /r "$INSTDIR"

    ; Remove shortcuts
    Delete "$DESKTOP\2048 Hexa Game.lnk"
    Delete "$SMPROGRAMS\Hexa Games\2048 Hexa Game.lnk"
    RMDir "$SMPROGRAMS\Hexa Games"

    ; Remove registry keys
    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\2048HexaGame"
SectionEnd
