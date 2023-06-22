# Fun Project

Fun Project is a Java-based console application that moves your mouse pointer around your screen to keep the system active till a specific time.

## Getting Started
Clone the repository using git:
```shell
git clone https://github.com/username/FunProject.git
```

## Prerequisites
- Java JDK 11 or later
- Apache Maven

## Building
Navigate to the project directory and execute:
```shell
mvn clean install
```


## Running

Navigate to the `target` directory and execute:
```shell
java -jar fun-project-1.0.0.jar -e 18:00 -k
```

This will run the program until 6pm and keep the system active.

## Options
- Use `-e` or `--end-time` followed by the time in HH:mm format to set the end time for the program.
- Use `-k` or `--keep-alive` to keep the system active.

## License
This project is licensed under the Apache License. See the `LICENSE` file for details.
