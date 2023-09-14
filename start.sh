#!/bin/bash
jarfile=target/fun-project.jar
libFolder=target/lib/*
pluginFolder=target/plugins/*

if [[ -f "$jarfile" ]]; then
    echo "Running fun-project.jar"
    java -cp "$libFolder:$pluginFolder:$jarfile" com.aldrineeinsteen.fun.Main --signature --keep-alive
 else
    echo "fun-project.jar not found, building with Maven"
    ./mvnw clean install
    if [[ -f "$jarfile" ]]; then
        echo "Running fun-project.jar"
        java -cp "$libFolder:$pluginFolder:$jarfile" com.aldrineeinsteen.fun.Main --signature --keep-alive
    else
        echo "Error building jar file"
        exit 1
    fi
fi
