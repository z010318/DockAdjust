# DockAdjust — LSPosed source prototype

**This is source code, not an installable APK or flashable root module.** A GitHub Actions workflow is included to build a debug APK in a hosted Android build environment.

## Target
- OnePlus PLZ110, ColorOS 17.0.0.107 (Android 17 / API 37)
- Launcher package: `com.android.launcher`
- Candidate class: `com.android.launcher3.OplusHotseat`
- Candidate hook: `setTranslationY(float)`

## Features in this prototype
- Vertical offset setting from -120 to +120 dp.
- Negative values are intended to move upward; positive values downward.
- Slider, numeric input, save and reset controls.
- Hook is limited to `com.android.launcher`; no system APK or partition is modified.
- Disable the module in LSPosed to remove the hook.

## Build APK with GitHub Actions
1. Create a new empty GitHub repository.
2. Upload the contents of this folder to the repository's `main` branch.
3. Open **Actions** and run **Build DockAdjust APK** (or push a commit).
4. Open the completed workflow run and download the `DockAdjust-debug-apk` artifact. It contains `app-debug.apk`.
5. Install the APK, enable DockAdjust in LSPosed, and set its scope to **only** `com.android.launcher`.
6. Restart the launcher process (or reboot) after changing the scope or applying a setting.

The workflow uses Gradle 8.9, Android Gradle Plugin 8.7.3, Android platform 35, and downloads the Xposed API dependency from its configured Maven repository.

## Not yet validated on device
The supplied launcher DEX supports the class/method name and signature used as the initial hook candidate. The following still require testing on the actual PLZ110 firmware:
- Whether `OplusHotseat` is a `View` instance at runtime on this build.
- Whether this hook is called for every relevant Dock layout/animation path.
- Whether the sign and usable range match the intended visual movement.
- Whether the LSPosed fork accepts the legacy Xposed module entry metadata.

This is an experimental first build. If the Dock does not move, disable the module in LSPosed and restart Launcher. Do not replace or patch the system Launcher APK.
