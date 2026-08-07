# Compilation Guide

FastAIBot is a lightweight Java orchestration module with zero heavy framework overhead. It relies on `FastAI`, `FastAIMemory`, `FastString`, and `FastCore`.

### Requirements
- Java 17 or higher
- Maven 3.8+

### Building
To compile and install the module locally into your Maven repository:
```bash
mvn clean install
```

To run the Demo:
```bash
run-demo.bat
```
or manually:
```bash
cd examples/Demo
mvn clean compile exec:java
```
