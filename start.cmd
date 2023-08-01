@echo off
SET jarfile=target\fun-project.jar

SET end_time=%1
IF "%end_time%"=="" SET end_time=17:00

IF EXIST %jarfile% (
    echo Running fun-project.jar
    java -jar %jarfile% --end-time %end_time% --keep-alive --signature
) ELSE (
    echo fun-project.jar not found, building with Maven
    mvnw clean install
    IF EXIST %jarfile% (
        echo Running fun-project.jar
        java -jar %jarfile% --end-time %end_time% --keep-alive --signature
    ) ELSE (
        echo Error building jar file
        exit /b 1
    )
)
