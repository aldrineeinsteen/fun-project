#!/bin/bash
jarfile=target/fun-project.jar  
libFolder=target/lib/*
pluginFolder=target/plugins/*

# Parse command line arguments, default to interactive mode with dynamic plugin discovery
ARGS="$@"
if [ -z "$ARGS" ]; then
    echo "No arguments provided. Running in interactive mode with dynamic plugin discovery."
    echo "Available CLI options will be determined by loaded plugins."
    ARGS=""
fi

if [[ -f "$jarfile" ]]; then
    echo "Running fun-project.jar with arguments: $ARGS"
    java -cp "$libFolder:$pluginFolder:$jarfile" com.aldrineeinsteen.fun.Main $ARGS
 else
    echo "fun-project.jar not found, building with Maven"
    ./mvnw clean install
    if [[ -f "$jarfile" ]]; then
        echo "Running fun-project.jar with arguments: $ARGS"
        java -cp "$libFolder:$pluginFolder:$jarfile" com.aldrineeinsteen.fun.Main $ARGS
    else
        echo "Error building jar file"
        exit 1
    fi
fi
