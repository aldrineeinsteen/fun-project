@echo off
SET jarfile=target\fun-project.jar
SET libFolder=target\lib\*
SET pluginFolder=target\plugins\*

IF EXIST %jarfile% (
    echo Running fun-project.jar
    java -cp "%jarfile%;%libFolder%;%pluginFolder%" com.aldrineeinsteen.fun.Main --keep-alive --signature
) ELSE (
    echo fun-project.jar not found, building with Maven
    mvnw clean install
    IF EXIST %jarfile% (
        echo Running fun-project.jar
        java -cp "%jarfile%;%libFolder%;%pluginFolder%" com.aldrineeinsteen.fun.Main --keep-alive --signature
    ) ELSE (
        echo Error building jar file
        exit /b 1
    )
)
