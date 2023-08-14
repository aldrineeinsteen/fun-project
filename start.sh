#!/bin/bash
jarfile=target/fun-project-uber-jar-with-dependencies.jar

end_time=${1:-17:30}

if [[ -f "$jarfile" ]]; then
    echo "Running fun-project-uber-jar-with-dependencies.jar"
    java -jar $jarfile --end-time "$end_time" --keep-alive --signature
else
    echo "fun-project-uber-jar-with-dependencies.jar not found, building with Maven"
    ./mvnw clean install
    if [[ -f "$jarfile" ]]; then
        echo "Running fun-project-uber-jar-with-dependencies.jar"
        java -jar $jarfile --end-time "$end_time" --keep-alive --signature
    else
        echo "Error building jar file"
        exit 1
    fi
fi
