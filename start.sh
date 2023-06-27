#!/bin/bash
jarfile=target/fun-project.jar

if [ -f "$jarfile" ]
then
    echo "Running fun-project.jar"
    java -jar $jarfile
else
    echo "fun-project.jar not found, building with Maven"
    mvnw clean install
    if [ -f "$jarfile" ]
    then
        echo "Running fun-project.jar"
        java -jar $jarfile
    else
        echo "Error building jar file"
        exit 1
    fi
fi
