# 🚨 CRITICAL: Must Change Before Release!

## ⚠️ WARNING: TEST MODE IS CURRENTLY ENABLED!

Your app is running in **TEST MODE** which means it can work without a valid Bright Data App ID. This is for development only.

---

## 🔴 REQUIRED CHANGE #1: Disable Test Mode

**File:** `composeApp/src/desktopMain/kotlin/com/alexjlockwood/twentyfortyeight/brightsdk/internal/WindowsBrightDataSdk.kt`

**Line 85 - CHANGE THIS:**
```kotlin
nativeLib.brd_sdk_set_test_mode_c(1)  // ❌ TEST MODE ENABLED!
```

**TO THIS:**
```kotlin
nativeLib.brd_sdk_set_test_mode_c(0)  // ✅ PRODUCTION MODE
```

---

## 🟠 REQUIRED CHANGE #2: Verify Your App ID

**File:** Same file, Line 79

**Current value:**
```kotlin
nativeLib.brd_sdk_set_appid_c("win_hexabrain_systems_pvt_ltd.2048_game")
```

**Action Required:**
1. ✅ If this IS your official App ID from Bright Data → You're good!
2. ❌ If this is a placeholder → Contact Bright Data to get your official App ID

**How to get an official App ID:**
- Contact Bright Data support or your account manager
- Register your application in their dashboard
- They will provide you with an App ID like: `win_yourcompany.yourapp`

---

## 🟢 After Making Changes

1. **Build for release:**
   ```bash
   cd D:\Hexabrain\Other\compose-multiplatform-2048
   gradlew clean
   gradlew :composeApp:packageDistributionForCurrentOS
   ```

2. **Find your installer:**
   - Windows .exe: `composeApp/build/compose/binaries/main/exe/`
   - Windows .msi: `composeApp/build/compose/binaries/main/msi/`

3. **Test on a clean machine** before distributing!

---

## 📝 Quick Checklist

- [ ] Changed test mode from `1` to `0` ✅
- [ ] Verified App ID with Bright Data ✅
- [ ] Tested the release build ✅
- [ ] Verified consent dialog appears correctly ✅
- [ ] Ready to distribute! 🎉

---

**Read the full checklist:** `RELEASE_CHECKLIST.md`
