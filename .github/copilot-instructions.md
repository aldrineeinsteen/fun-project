# Copilot Instructions for Fun Project

## Project Overview

Fun Project is a Java-based console application that moves your mouse pointer to keep the system active until a specified time. It features a modular plugin architecture with a Maven multi-module structure.

## Architecture & Key Components

### Multi-Module Structure
- **`fun-core/`**: Main application entry point (`com.aldrineeinsteen.fun.Main`)
- **`plugins/`**: Modular plugins with shared interfaces
  - `plugin-core/`: Base interfaces (`PluginTemplate`, singleton pattern)
  - `utility-core/`: Utility base classes (`UtilityTemplate` for runnables)
  - `keep-alive-timer/`: Mouse movement plugin
  - `signature-selector/`: Clipboard signature selection plugin

### Dynamic Plugin System
Plugins are **fully dynamic** - discovered, loaded, and executed at runtime via YAML configuration (`plugin.yaml`) in resources:
```yaml
name: PluginName
pluginClass: com.package.ClassName
option:
  - shortOpt: k
    longOpt: keep-alive
    description: Description text
    hasArguments: false
    required: false
params:
  - shortOpt: e
    longOpt: end-time
    description: End Time in format of HH:mm
    hasArguments: true
    required: false
shortcuts:
  - key: "CTRL + SHIFT + ALT + S"
    action: "methodName"
```

**Dynamic Loading Process:**
1. `PluginRepository.init()` scans classpath for ALL `plugin.yaml` files
2. Plugin classes are instantiated via reflection (`Class.forName()`)
3. CLI options are dynamically registered with Apache Commons CLI
4. Global hotkeys are dynamically mapped to plugin actions
5. Plugins execute via dynamic method dispatch (`executeAction()`)

Plugin classes extend `PluginTemplate` (singleton) or `UtilityTemplate` (runnable). The system supports **zero-code plugin discovery** - simply add a JAR with `plugin.yaml` to the classpath.

**Plugin Self-Sufficiency**: Each plugin is a complete, standalone module with its own configuration, resources, and functionality. No inter-plugin dependencies exist.

## Build & Development Workflows

### Build Commands
```bash
# Standard build
./mvnw clean install

# Build outputs:
# - target/fun-project.jar (main application)
# - target/lib/* (dependencies)
# - target/plugins/* (plugin JARs)
```

### Running the Application
Use classpath pattern: `java -cp "target/lib/*:target/plugins/*:target/fun-project.jar" com.aldrineeinsteen.fun.Main [options]`

The `start.sh` script automates this process.

### Testing
- JUnit 5 + Mockito for unit tests
- JaCoCo for coverage reports (`target/site/jacoco/`)
- Tests require X11 display (use `xvfb-run` in headless environments)

## Key Patterns & Conventions

### Logging
- SLF4J + Logback configuration in `logback.xml` 
- File logging to `logs/fun-project-YYYY-MM-dd.log` (5-day rotation)
- Pattern: `%date %level [%thread] %logger{10} [%file:%line] %msg%n`

### Configuration Loading
- YAML-based configuration using SnakeYAML
- Plugin resources loaded via `getClass().getClassLoader().getResourceAsStream()`
- Weighted selection algorithms (see `SignatureSelector`)

### Dependency Management
- Parent POM defines common dependencies and properties
- Custom GitHub Packages repository in `settings.xml`
- Version management via `version.final` and `version.dev` properties

### Release Process
- GitHub Actions for CI/CD (`.github/workflows/`)
- Maven Release Plugin with automated tagging
- Publish to GitHub Packages

## Command Line Interface

Uses Apache Commons CLI with dynamic option registration from plugins:
- `-k/--keep-alive`: Activate mouse movement
- `-e/--end-time`: Set end time (HH:mm format) 
- `-sec/--seconds`: Override delay interval
- `-sign/--signature`: Enable signature selector
- Global hotkeys supported (e.g., `Ctrl+Shift+Alt+S`)

## Dynamic Plugin Architecture Details

### True Dynamic Loading
- **Runtime Discovery**: Scans entire classpath for `plugin.yaml` files via `ClassLoader.getResources()`
- **Reflection-Based**: Plugin classes instantiated dynamically using `Class.forName()` and reflection
- **No Hardcoding**: Zero references to specific plugin classes in core application
- **CLI Integration**: Options dynamically parsed from YAML and registered with Apache Commons CLI
- **Hotkey Mapping**: Global shortcuts dynamically bound to plugin actions via `jnativehook`

### Plugin Development Workflow
1. Create plugin class extending `PluginTemplate` or `UtilityTemplate`
2. Add `plugin.yaml` to `src/main/resources/` with plugin metadata
3. Build and place JAR in `target/plugins/` - **automatically discovered at runtime**
4. No core application changes needed

### Self-Sufficient Plugin Architecture
**Each plugin is completely self-contained:**
- **Independent Configuration**: Plugin defines its own CLI options, parameters, and shortcuts in `plugin.yaml`
- **Isolated Dependencies**: Plugin manages its own resource files and dependencies (e.g., `signatures.yaml`)
- **Autonomous Execution**: Plugin handles its own lifecycle, initialization, and business logic
- **No Cross-Dependencies**: Plugins don't depend on each other - only on base interfaces
- **Pluggable Deployment**: Drop plugin JAR into classpath - no registration or configuration needed

### Key Implementation Classes
- `PluginRepository`: Central plugin registry with dynamic loading logic
- `GlobalInputListener`: Handles dynamic hotkey-to-plugin action mapping
- `PluginTemplate`: Base class with singleton pattern for stateful plugins
- `UtilityTemplate`: Base class for runnable utilities

## Development Notes

- Java 21+ required
- Mouse control via `java.awt.Robot`
- Global hotkey handling via `jnativehook` library
- Terminal interaction through JLine for raw mode input
- Plugin instantiation uses reflection with singleton pattern
- Cross-platform support (Windows/Unix shell scripts)

**Critical**: The system is truly plugin-driven - adding new functionality requires only creating a plugin JAR with proper `plugin.yaml`, no core code changes.

**Git Flow**: Follow feature branching and pull request workflow for contributions. Also ensure to have a release branch before raising PR to main, and also any raise PR via GH CLI.