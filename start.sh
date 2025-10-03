#!/bin/bash

jarfile=target/fun-project.jar
libFolder=target/lib/*
pluginFolder=target/plugins/*

# Initialize optional parameters with defaults
signature="--signature"
keep_alive="--keep-alive"  # Default value for keep-alive
end_time=""

# Parse optional arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --signature)
      signature="--signature"
      shift # past argument
      ;;
    --keep-alive)
      keep_alive="--keep-alive"
      shift # past argument
      ;;
    --end-time)
      end_time="--end-time $2"
      shift # past argument
      shift # past value
      ;;
    *)    # unknown option
      shift # past argument
      ;;
  esac
done

if [[ -f "$jarfile" ]]; then
    echo "Running fun-project.jar"
    java -cp "$libFolder:$pluginFolder:$jarfile" com.aldrineeinsteen.fun.Main $signature $keep_alive $end_time
else
    echo "fun-project.jar not found, building with Maven"
    ./mvnw clean install
    if [[ -f "$jarfile" ]]; then
        echo "Running fun-project.jar"
        java -cp "$libFolder:$pluginFolder:$jarfile" com.aldrineeinsteen.fun.Main $signature $keep_alive $end_time
    else
        echo "Error building jar file"
        exit 1
    fi
fi