# Fun Project

Fun Project is a Java-based console application that moves your mouse pointer around your screen to keep the system
active till a specified time.

## Getting Started

Clone the repository using git:

```shell
git clone https://github.com/username/FunProject.git
```

## Prerequisites

- Java JDK 21 or later
- Apache Maven

## Building and Running

The project includes a batch script for Windows (`run.bat`) and a shell script for Unix-like systems (`run.sh`) to
simplify the building and running process.
These scripts will first check if `target/fun-project.jar` exists. If the jar file exists, it
will be run directly with the proper classpath. If the jar file doesn't exist, the project will be built using Maven and then the jar file will be
run.
You can pass an end time to the script, which will be passed to the jar file when it's run. If you don't pass an end
time, it will default to '17:00'.

By default, the application moves the mouse pointer by one pixel every 30 seconds. This can be overridden; please refer
to [Options](#options)

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

Replace `[end-time]` with your desired end time, like '18:00'. If you don't provide an end time, '17:00' will be used by
default.

### Manual Build

Navigate to the project directory and execute:

```shell
mvnw clean install
```

### Manual Run

From the project root directory, execute:

```shell
java -cp "target/lib/*:target/plugins/*:target/fun-project.jar" com.aldrineeinsteen.fun.Main -e 18:00 -k -sign
```

On Windows, use semicolons instead of colons:

```shell
java -cp "target/lib/*;target/plugins/*;target/fun-project.jar" com.aldrineeinsteen.fun.Main -e 18:00 -k -sign
```

This will run the program until 6pm and keep the system active.

## Options

- Use `-e` or `--end-time` followed by the time in HH:mm format to set the end time for the program.
- Use `-k` or `--keep-alive` to keep the system active.
- Use `-s` or `--seconds` to configure the seconds on top of keep-alive timer.
- Use `-sign` or `--signature` to configure the quick tool - Signature Selector.
- Use `--dash` or `--dashboard` to enable the TUI dashboard mode (see [Dashboard Mode](#dashboard-mode) below).

## Dashboard Mode

The application now supports a TUI (Text User Interface) dashboard that provides real-time status information in the console. This replaces the traditional log output with a clean, continuously updating display.

### Enabling Dashboard Mode

To enable the dashboard, use the `--dash` or `--dashboard` flag:

```shell
java -cp "target/lib/*:target/plugins/*:target/fun-project.jar" com.aldrineeinsteen.fun.Main -k -e 18:00 --dash
```

Or with the run scripts:

```shell
./run.sh 18:00 --dash
```

On Windows:

```shell
java -cp "target/lib/*;target/plugins/*;target/fun-project.jar" com.aldrineeinsteen.fun.Main -k -e 18:00 --dash
```

### Dashboard Display

When dashboard mode is enabled, you'll see a real-time display like this:

```
FunProject v1.2.46
────────────────────────────────────────────────────────
Current Time: 14:23:45

End Time: 18:00
Time Remaining: 3h 36m 15s
Next Move: 30s
Mode: jitter
Status: Running...
Activity: ████████░░

────────────────────────────────────────────────────────
Press Ctrl+C to exit
```

### Dashboard Features

- **Real-time Updates**: The dashboard refreshes every second to show current status
- **Time Tracking**: Shows remaining time until the configured end time
- **Activity Indicator**: Visual progress bar showing activity
- **Plugin Support**: Each plugin can contribute its own data to the dashboard
- **Clean Display**: Uses ANSI escape codes for a clean, flicker-free display

### Plugin Dashboard Integration

Plugins can contribute to the dashboard by implementing the `DashboardRenderer` interface and configuring dashboard settings in their `plugin.yaml`:

```yaml
dashboard:
  enabled: true
  position: 10
```

The `position` field determines the display order (lower numbers appear first).

## License

This project is licensed under the Apache License, Version 2.0. See the `LICENSE` file for details.

Copyright 2017-2025 Aldrine Einsteen