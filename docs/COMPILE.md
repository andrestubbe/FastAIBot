# Compilation Guide

FastBot is a pure Java module with no native JNI dependencies (unlike FastTerminal or FastGPU). It relies solely on `FastAI` and `FastAIMemory`.

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
cd examples/Demo
mvn clean compile exec:java
```
