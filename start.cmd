@echo off
SET jarfile=target\fun-project.jar

IF EXIST %jarfile% (
    echo Running fun-project.jar
    java -jar %jarfile%
) ELSE (
    echo fun-project.jar not found, building with Maven
    mvnw clean install
    IF EXIST %jarfile% (
        echo Running fun-project.jar
        java -jar %jarfile%
    ) ELSE (
        echo Error building jar file
        exit /b 1
    )
)
