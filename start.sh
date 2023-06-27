#!/bin/bash
jarfile=target/fun-project.jar

end_time=${1:-17:30}

if [[ -f "$jarfile" ]]; then
    echo "Running fun-project.jar"
    java -jar $jarfile --end-time $end_time --keep-alive
else
    echo "fun-project.jar not found, building with Maven"
    ./mvnw clean install
    if [[ -f "$jarfile" ]]; then
        echo "Running fun-project.jar"
        java -jar $jarfile --end-time $end_time --keep-alive
    else
        echo "Error building jar file"
        exit 1
    fi
fi