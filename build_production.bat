@echo off
setlocal enabledelayedexpansion

echo =======================================================
echo   MENE MONITOR - Enterprise Production Build
echo =======================================================

rem Clear environment variable conflict for AGP
set ANDROID_PREFS_ROOT=

rem Fallback to Android Studio JBR if JAVA_HOME is not set
if exist "C:\Program Files\Android\Android Studio2\jbr" (
    if not defined JAVA_HOME set "JAVA_HOME=C:\Program Files\Android\Android Studio2\jbr"
)

echo.
echo [1/4] Cleaning previous build artifacts...
call gradlew clean --no-configuration-cache 2>nul || rmdir /s /q "app\build" 2>nul

echo.
echo [2/4] Compiling Kotlin Release Code...
call gradlew :app:compileReleaseKotlin --no-configuration-cache
if %errorlevel% neq 0 goto build_failed

echo.
echo [3/4] Compiling Production APK (R8 Minified ^& ProGuard Shrink)...
call gradlew assembleRelease -x test --no-configuration-cache
if %errorlevel% neq 0 goto build_failed

echo.
echo [4/4] Generating Production Google Play App Bundle (.aab)...
call gradlew bundleRelease -x test --no-configuration-cache
if %errorlevel% neq 0 goto build_failed

echo.
echo =======================================================
echo   BUILD SUCCESSFUL!
echo.
echo   Google Play Upload Bundle (.aab):
echo     app\build\outputs\bundle\release\app-release.aab
if exist "app\build\outputs\bundle\release\app-release.aab" (
    for %%F in ("app\build\outputs\bundle\release\app-release.aab") do echo     Size: %%~zF bytes
)
echo.
echo   Direct Install APK (.apk):
echo     app\build\outputs\apk\release\app-release.apk
if exist "app\build\outputs\apk\release\app-release.apk" (
    for %%F in ("app\build\outputs\apk\release\app-release.apk") do echo     Size: %%~zF bytes
)
echo.
echo   De-obfuscation Mapping File (Save for Play Console):
echo     app\build\outputs\mapping\release\mapping.txt
echo =======================================================
echo.
pause
exit /b 0

:build_failed
echo.
echo =======================================================
echo   ERROR: Production build failed! Check errors above.
echo =======================================================
echo.
pause
exit /b 1
