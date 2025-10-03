@echo off

set "jarfile=target\fun-project.jar"
set "libFolder=target\lib\*"
set "pluginFolder=target\plugins\*"

REM Initialize optional parameters with defaults
set "signature=--signature"
set "keep_alive=--keep-alive"
set "end_time="

REM Parse optional arguments
:parse_args
if "%~1"=="" goto after_args
if "%~1"=="--signature" (
    set "signature=--signature"
    shift
    goto parse_args
)
if "%~1"=="--keep-alive" (
    set "keep_alive=--keep-alive"
    shift
    goto parse_args
)
if "%~1"=="--end-time" (
    set "end_time=--end-time %~2"
    shift
    shift
    goto parse_args
)
shift
goto parse_args

:after_args
if exist "%jarfile%" (
    echo Running fun-project.jar
    java -cp "%libFolder%;%pluginFolder%;%jarfile%" com.aldrineeinsteen.fun.Main %signature% %keep_alive% %end_time%
) else (
    echo fun-project.jar not found, building with Maven
    call mvnw.cmd clean install
    if exist "%jarfile%" (
        echo Running fun-project.jar
        java -cp "%libFolder%;%pluginFolder%;%jarfile%" com.aldrineeinsteen.fun.Main %signature% %keep_alive% %end_time%
    ) else (
        echo Error building jar file
        exit /b 1
    )
)