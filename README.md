# Fun Project
Fun Project is a Java-based console application that moves your mouse pointer around your screen to keep the system active till a specified time.

## Getting Started
Clone the repository using git:

```shell
git clone https://github.com/username/FunProject.git
```

## Prerequisites
- Java JDK 11 or later
- Apache Maven

## Building and Running
The project includes a batch script for Windows (`run.bat`) and a shell script for Unix-like systems (`run.sh`) to simplify the building and running process.
These scripts will first check if `target/fun-project.jar` exists. If the jar file exists, it will be run directly. If the jar file doesn't exist, the project will be built using Maven and then the jar file will be run.
You can pass an end time to the script, which will be passed to the jar file when it's run. If you don't pass an end time, it will default to '17:30'.

### On Windows
Run the batch file with the optional end time:
```shell
run.bat [end-time]
```

### On Unix-like systems
Make the shell script executable and then run it with the optional end time:
```shell
chmod +x run.sh
./run.sh [end-time]
```

Replace `[end-time]` with your desired end time, like '18:00'. If you don't provide an end time, '17:30' will be used by default.

### Manual Build
Navigate to the project directory and execute:

```shell
mvn clean install
```

### Manual Run
Navigate to the `target` directory and execute:

```shell
java -jar fun-project.jar -e 18:00 -k
```
This will run the program until 6pm and keep the system active.

## Options
- Use `-e` or `--end-time` followed by the time in HH:mm format to set the end time for the program.
- Use `-k` or `--keep-alive` to keep the system active.

## License
This project is licensed under the Apache License. See the `LICENSE` file for details.