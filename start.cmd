@echo off

REM Fun Project Launcher Script (Windows)
REM 
REM This script automatically detects the current branch and chooses the appropriate way to run the application:
REM - On 'main' branch: Downloads pre-built binaries from GitHub Releases (no authentication required)
REM - On other branches: Builds locally using Maven (for development)
REM
REM Benefits of using GitHub Releases:
REM - No authentication required (public releases)
REM - Faster startup (no build time)
REM - Includes all dependencies and plugins in a single download
REM - Works offline after first download

set "jarfile=target\fun-project.jar"
set "libFolder=target\lib\*"
set "pluginFolder=target\plugins\*"

REM Initialize optional parameters with defaults
set "signature=--sign"
set "keep_alive=-k"
set "end_time=-e 1830"
set "dashboard=--dash"

REM Parse optional arguments
:parse_args
if "%~1"=="" goto after_args
if "%~1"=="--signature" (
    set "signature=--sign"
    shift
    goto parse_args
)
if "%~1"=="--sign" (
    set "signature=--sign"
    shift
    goto parse_args
)
if "%~1"=="--keep-alive" (
    set "keep_alive=-k"
    shift
    goto parse_args
)
if "%~1"=="-k" (
    set "keep_alive=-k"
    shift
    goto parse_args
)
if "%~1"=="--dashboard" (
    set "dashboard=--dash"
    shift
    goto parse_args
)
if "%~1"=="--dash" (
    set "dashboard=--dash"
    shift
    goto parse_args
)
if "%~1"=="--end-time" (
    set "end_time=-e %~2"
    shift
    shift
    goto parse_args
)
if "%~1"=="-e" (
    set "end_time=-e %~2"
    shift
    shift
    goto parse_args
)
shift
goto parse_args

:after_args

REM Function to run the application
goto check_jar_and_run

:run_application
echo Running fun-project.jar
echo [%date% %time%] Starting application with args: %signature% %keep_alive% %dashboard% %end_time% >> runtime.log
java -cp "%libFolder%;%pluginFolder%;%jarfile%" com.aldrineeinsteen.fun.Main %signature% %keep_alive% %dashboard% %end_time% 2>&1 | tee -a runtime.log
if %errorlevel% neq 0 (
    echo [%date% %time%] Application exited with error code: %errorlevel% >> runtime.log
)
goto :eof

:build_locally
echo Building project locally with Maven...
echo [%date% %time%] Building project locally with Maven >> runtime.log
call mvnw.cmd clean install 2>&1 | tee -a runtime.log
if exist "%jarfile%" (
    if %errorlevel% neq 0 (
        echo [%date% %time%] Build completed with warnings (exit code: %errorlevel%) >> runtime.log
    )
    call :run_application
) else (
    echo Error: Failed to build jar file locally
    echo [%date% %time%] ERROR: Failed to build jar file locally >> runtime.log
    exit /b 1
)
goto :eof

:download_from_github_releases
echo Attempting to download latest release from GitHub Releases...
echo [%date% %time%] Attempting to download from GitHub Releases >> runtime.log

REM Create target directories
if not exist "target" mkdir target
if not exist "target\lib" mkdir target\lib
if not exist "target\plugins" mkdir target\plugins

REM GitHub repository details
set "REPO=aldrineeinsteen/fun-project"

REM Check if curl is available
curl --version >nul 2>&1
if %errorlevel% neq 0 (
    echo curl is required but not installed. Falling back to local build.
    echo [%date% %time%] ERROR: curl not found, falling back to local build >> runtime.log
    call :build_locally
    goto :eof
)

echo Fetching latest release information...

REM Get the latest release information and extract download URL
curl -s "https://api.github.com/repos/%REPO%/releases/latest" > temp_release.json 2>> runtime.log

REM Extract the download URL for distribution zip (simplified approach for Windows)
findstr /C:"fun-project-distribution.zip" temp_release.json > temp_url.txt
if %errorlevel% neq 0 (
    echo Distribution package not found in latest release. Falling back to local build.
    echo [%date% %time%] WARNING: Distribution package not found, falling back to local build >> runtime.log
    del temp_release.json 2>nul
    call :build_locally
    goto :eof
)

REM Parse the URL (this is a simplified version - in production you might want a more robust parser)
for /f "tokens=4 delims=:, " %%i in ('findstr "browser_download_url.*fun-project-distribution.zip" temp_release.json') do (
    set "DOWNLOAD_URL=%%~i"
)

REM Clean up the URL (remove quotes)
set "DOWNLOAD_URL=%DOWNLOAD_URL:"=%"

del temp_release.json 2>nul
del temp_url.txt 2>nul

if "%DOWNLOAD_URL%"=="" (
    echo Could not extract download URL. Falling back to local build.
    echo [%date% %time%] ERROR: Could not extract download URL, falling back to local build >> runtime.log
    call :build_locally
    goto :eof
)

echo Downloading distribution package from: %DOWNLOAD_URL%
echo [%date% %time%] Downloading from: %DOWNLOAD_URL% >> runtime.log

REM Download the distribution package
curl -L -o target\fun-project-distribution.zip "%DOWNLOAD_URL%" 2>> runtime.log
if %errorlevel% neq 0 (
    echo Failed to download from GitHub Releases. Falling back to local build.
    echo [%date% %time%] ERROR: Download failed, falling back to local build >> runtime.log
    call :build_locally
    goto :eof
)

echo Download successful. Extracting...
echo [%date% %time%] Download successful, extracting... >> runtime.log

REM Extract the distribution (Windows has built-in zip support via PowerShell)
powershell -command "Expand-Archive -Path 'target\fun-project-distribution.zip' -DestinationPath 'target' -Force" 2>> runtime.log
if %errorlevel% neq 0 (
    echo Failed to extract package. Falling back to local build.
    echo [%date% %time%] ERROR: Extraction failed, falling back to local build >> runtime.log
    call :build_locally
    goto :eof
)

del target\fun-project-distribution.zip 2>nul

REM Verify the main jar exists
if exist "%jarfile%" (
    echo Successfully downloaded and extracted from GitHub Releases
    echo [%date% %time%] Successfully extracted from GitHub Releases >> runtime.log
    call :run_application
) else (
    echo Main jar not found in downloaded package. Falling back to local build.
    echo [%date% %time%] ERROR: Main jar not found after extraction, falling back to local build >> runtime.log
    call :build_locally
)
goto :eof

:check_jar_and_run
REM Get current branch
for /f %%i in ('git branch --show-current 2^>nul') do set "current_branch=%%i"
if "%current_branch%"=="" set "current_branch=unknown"

echo [%date% %time%] ========== Fun Project Startup ========== >> runtime.log
echo [%date% %time%] Current branch: %current_branch% >> runtime.log

if exist "%jarfile%" (
    echo Found existing jar file
    echo [%date% %time%] Found existing jar file >> runtime.log
    call :run_application
) else if "%current_branch%"=="main" (
    echo On main branch - attempting to use GitHub Releases
    echo [%date% %time%] On main branch - attempting GitHub Releases >> runtime.log
    call :download_from_github_releases
) else (
    echo On development branch (%current_branch%^) - building locally
    echo [%date% %time%] On development branch (%current_branch%) - building locally >> runtime.log
    call :build_locally
)