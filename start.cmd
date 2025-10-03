@echo off
SET jarfile=target\fun-project.jar
SET libFolder=target\lib\*
SET pluginFolder=target\plugins\*

REM Parse command line arguments, default to interactive mode with dynamic plugin discovery
SET ARGS=%*
IF "%ARGS%"=="" (
    echo No arguments provided. Running in interactive mode with dynamic plugin discovery.
    echo Available CLI options will be determined by loaded plugins.
)

IF EXIST %jarfile% (
    echo Running fun-project.jar with arguments: %ARGS%
    java -cp "%jarfile%;%libFolder%;%pluginFolder%" com.aldrineeinsteen.fun.Main %ARGS%
) ELSE (
    echo fun-project.jar not found, building with Maven
    mvnw clean install
    IF EXIST %jarfile% (
        echo Running fun-project.jar with arguments: %ARGS%
        java -cp "%jarfile%;%libFolder%;%pluginFolder%" com.aldrineeinsteen.fun.Main %ARGS%
    ) ELSE (
        echo Error building jar file
        exit /b 1
    )
)
