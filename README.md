# FastAIBot 0.1.2 [ALPHA-2026-08-07]: High-Performance Bot Orchestrator for Java

[![Status](https://img.shields.io/badge/status-0.1.2-brightgreen.svg)](https://github.com/andrestubbe/FastAIBot/releases/tag/0.1.2)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-0.1.2-green.svg)](https://jitpack.io/#andrestubbe/FastAIBot)

---

**⚡ A zero-latency, asynchronous orchestration runtime connecting LLM brains and conversation memory.**

**FastAIBot** is the conversational orchestrator of the **FastJava** ecosystem. It bridges the stateless AI generation of **[FastAI](https://github.com/andrestubbe/FastAI)** with persistent state (**[FastAIMemory](https://github.com/andrestubbe/FastAIMemory)**) and native zero-allocation SIMD response buffering via **[FastString](https://github.com/andrestubbe/FastString)**.

[**Watch Demo (YouTube)**](https://youtu.be/Om9eVAcbSA8)

[![FastAIBot Showcase](docs/screenshot.png)](https://youtu.be/Om9eVAcbSA8)

---

## Quick Start

```java
import fastaibot.FastAIBot;
import fastai.FastAI;
import fastai.AI;
import java.util.function.Consumer;

public class Demo {
    public static void main(String[] args) {
        // 1. Connect the Brain
        AI brain = FastAI.connect("gemini:gemini-2.5-flash", System.getenv("GEMINI_API_KEY"));

        // 2. Define Text Output Consumer
        Consumer<String> textOut = text -> System.out.print(text);

        // 3. Boot the Bot with System Persona
        FastAIBot bot = new FastAIBot(brain, "You are a helpful AI assistant.", textOut);

        // 4. Talk (Streams instantly, records into ConversationHistory)
        bot.streamChat("Are you a bot in a monitor?");
    }
}
```

---

## Table of Contents

- [Why FastAIBot?](#why-fastaibot)
- [Quick Start](#quick-start)
- [Key Features](#key-features)
- [Real-World Use Cases](#real-world-use-cases)
- [Architecture Overview](#architecture-overview)
- [API Quick Reference](#api-quick-reference)
- [Technical Demos & Benchmarks](#technical-demos--benchmarks)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [Related Projects](#related-projects)
- [License](#license)

---

## Why FastAIBot?

Traditional conversational AI frameworks in Java (like LangChain4j or Spring AI bot harnesses) add heavy abstraction layers, repetitive JSON parsing, and garbage collector pressure that introduce noticeable jitter to real-time chat interactions:

- **Heavyweight Framework Abstractions**: Wrapping every turn in complex session objects and reflection-heavy middleware adds 15–40 ms of pipeline lag before streaming even starts.
- **Heap Allocation Overhead**: Reallocating Java `StringBuilder` buffers on every token generates high memory churn and triggers GC pauses during prolonged conversations.
- **Tightly Coupled Brain and State**: Most frameworks bind prompt generation directly to specific model drivers, making it impossible to switch models without losing memory formats.

FastAIBot solves this by separating pure stateless inference from stateful conversation flow:

- **Zero-Latency Streaming**: Passes incoming tokens directly to your UI consumer as they arrive without intermediate buffering delays.
- **Zero-Allocation SIMD Buffering**: Utilizes native `FastString` memory to assemble full turns without GC heap churn.
- **Clean Brain/Memory Decoupling**: Composes stateless `FastAI` clients with structured `FastAIMemory` contexts.

| Feature | Standard Chat Frameworks (LangChain4j / Spring AI) | FastAIBot |
|:---|:---|:---|
| **Memory Architecture** | Generic boxed objects & heap allocation | Direct `ConversationHistory` with native `FastString` buffer |
| **Response Buffering** | Java `StringBuilder` reallocations | Pre-allocated native SIMD UTF-8 memory buffer |
| **Streaming Latency** | Intercepted by middleware interceptors | Direct callback forwarding directly from FastAI SSE stream |
| **Framework Overhead** | 10+ transitive dependencies, heavyweight DI | Zero-dependency core, instant startup (<50 ms) |
| **Mind / Memory Coupling**| Tightly coupled state & model logic | Clean decoupled composition: Brain (`FastAI`) + Memory (`FastAIMemory`) |

---

## Key Features

- ⚡ **Zero-Latency Streaming**: Passes incoming tokens directly to your UI consumer as they arrive without intermediate buffering delays.
- 🧠 **Context-Aware Conversations**: Automatically appends turns into structured `ConversationHistory` for seamless multi-turn memory.
- 🚀 **Zero-Allocation Buffering**: Utilizes `FastString` native off-heap / SIMD buffers to assemble complete turns without GC heap churn.
- 🧩 **Modular Formatter Support**: Easily plug in custom `MemoryFormatter` strategies (e.g. ChatML, Llama-3 headers, Markdown).
- 📦 **Minimal Footprint**: Clean, lightweight architecture that fits into any CLI, Swing, JavaFX, or headless bot harness.

---

## Real-World Use Cases

- 🤖 **Interactive Desktop Assistants**: Power desktop conversational companion bots with streaming responses directly to terminal TUIs or UI overlays.
- 🎮 **In-Game NPCs & Companion Bots**: Drive in-game conversational entities with continuous context history and minimal memory impact on the main game loop.
- 💬 **Customer Service Automation**: Handle stateful multi-turn customer dialogues with structured memory formatting and automatic history persistence.
- 🛠️ **Developer Chat Consoles**: Embed interactive LLM-driven developer consoles inside IDE tools or CLI automation harnesses.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         FastAIBot                           │
│                 (Conversational Orchestrator)               │
└──────────────┬───────────────────────────────┬──────────────┘
               │ 1. Context Prompt             │ 2. Stream Tokens
               ▼                               ▼
┌───────────────────────────────┐       ┌───────────────────────────────┐
│          FastAIMemory         │       │             FastAI            │
│   • ConversationHistory       │       │   • Streaming SSE Pipeline    │
│   • MemoryContextBuilder      │       │   • 20+ LLM Providers         │
└───────────────────────────────┘       └──────────────┬────────────────┘
                                                       │ Token Append
                                                       ▼
                                        ┌───────────────────────────────┐
                                        │           FastString          │
                                        │   • Native SIMD Buffer        │
                                        │   • Zero Heap Allocation      │
                                        └───────────────────────────────┘
```

---

## API Quick Reference

| Constructor / Method | Return Type | Description | Docs |
|:---|:---|:---|:---|
| `FastAIBot(ai, systemPrompt, textOut)` | `FastAIBot` | Initializes bot with default `PlainTextFormatter` and 64 KB native buffer. | [Reference](docs/REFERENCE.md) |
| `FastAIBot(ai, systemPrompt, textOut, formatter)` | `FastAIBot` | Initializes bot with a custom `MemoryFormatter`. | [Reference](docs/REFERENCE.md) |
| `FastAIBot(ai, systemPrompt, textOut, formatter, bufferBytes)` | `FastAIBot` | Initializes bot with custom formatter and initial native buffer capacity. | [Reference](docs/REFERENCE.md) |
| `streamChat(userInput)` | `void` | Executes a multi-turn turn: appends input, builds context, streams tokens, and saves reply. | [Reference](docs/REFERENCE.md) |
| `getHistory()` | `ConversationHistory` | Returns the active conversation history backing this bot instance. | [Reference](docs/REFERENCE.md) |

---

## Technical Demos & Benchmarks

| Case | Java Example | Launcher | Description |
|:---|:---|:---|:---|
| **Interactive Console Bot** | [Demo.java](examples/Demo/src/main/java/demo/Demo.java) | `run-demo.bat` | Interactive terminal chat loop with streaming output and multi-turn memory. |
| **JMH Microbenchmark Suite** | [Benchmark.java](examples/Benchmark/src/main/java/fastaibot/benchmark/Benchmark.java) | `run-benchmark.bat` | JMH throughput benchmark measuring initialization speed and prompt formatting latency. |

---

## Installation

### Option 1: Maven (Recommended)

Add the JitPack repository and the dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <!-- FastAIBot - Conversational Orchestrator -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastaibot</artifactId>
        <version>0.1.2</version>
    </dependency>

    <!-- FastAI - Unified AI Client -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAI</artifactId>
        <version>0.1.14</version>
    </dependency>

    <!-- FastAIMemory - Conversation Memory -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastAIMemory</artifactId>
        <version>0.1.3</version>
    </dependency>

    <!-- FastString - SIMD String Buffer -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastString</artifactId>
        <version>0.1.0</version>
    </dependency>

    <!-- FastCore - Required Native Loader -->
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>FastCore</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

### Option 2: Gradle (via JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fastaibot:0.1.2'
    implementation 'com.github.andrestubbe:FastAI:0.1.14'
    implementation 'com.github.andrestubbe:FastAIMemory:0.1.3'
    implementation 'com.github.andrestubbe:FastString:0.1.0'
    implementation 'com.github.andrestubbe:FastCore:0.1.0'
}
```

### Option 3: Direct Download (No Build Tool)

Download the required release JARs directly to add them to your classpath:

1. 🤖 **[fastaibot-0.1.2.jar](https://github.com/andrestubbe/FastAIBot/releases/tag/0.1.2)** (Bot Orchestrator)
2. ⚡ **[FastAI-0.1.14.jar](https://github.com/andrestubbe/FastAI/releases/tag/0.1.14)** (Unified AI Client)
3. 🧠 **[FastAIMemory-0.1.3.jar](https://github.com/andrestubbe/FastAIMemory/releases/tag/0.1.3)** (Context & History)
4. 🚀 **[FastString-0.1.0.jar](https://github.com/andrestubbe/FastString/releases/tag/0.1.0)** (Zero-Copy Buffer)
5. ⚙️ **[FastCore-0.1.0.jar](https://github.com/andrestubbe/FastCore/releases/tag/0.1.0)** (Mandatory Native JNI Loader)

> [!IMPORTANT]
> All JARs must be included in your classpath for the native JNI bindings and conversation context pipelines to function correctly.

---

## Documentation

- **[REFERENCE.md](docs/REFERENCE.md)**: Full API contracts, constructor overloads, and routing logic.
- **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: Zero-latency orchestrator architecture rationale.
- **[COMPILE.md](docs/COMPILE.md)**: Maven build instructions.
- **[CHANGELOG.md](docs/CHANGELOG.md)**: Complete version history and release notes.
- **[ROADMAP.md](docs/ROADMAP.md)**: Planned milestones and future features.

---

## Platform Support

| Platform | Architecture | Status | Notes |
|:---|:---:|:---:|:---|
| **Windows 10 / 11** | x64 | ✅ Fully Supported | Full support with SIMD native buffer acceleration |
| **Linux** | x64 / AArch64 | 🚧 Planned | Pure Java fallback works; native SIMD buffer pending |
| **macOS** | Apple Silicon / x64 | 🚧 Planned | Pure Java fallback works; native SIMD buffer pending |

---

## Related Projects

- **[`FastAI`](https://github.com/andrestubbe/FastAI)**: Unified AI Client for Java (20+ providers)
- **[`FastAIAgent`](https://github.com/andrestubbe/FastAIAgent)**: Autonomous ReAct Agent Loop and Cognitive Mind
- **[`FastAIMemory`](https://github.com/andrestubbe/FastAIMemory)**: Conversation History, Sliding Windows, and Rolling Summaries
- **[`FastAIReasoner`](https://github.com/andrestubbe/FastAIReasoner)**: Deterministic Planning, Chain-of-Thought, and Self-Correction
- **[`FastAIRuntime`](https://github.com/andrestubbe/FastAIRuntime)**: Sandboxed Process Runner and Tool-Calling Execution Pipeline
- **[`FastString`](https://github.com/andrestubbe/FastString)**: Ultra-Fast Native SIMD String Operations for Java
- **[`FastCore`](https://github.com/andrestubbe/FastCore)**: Native Library Loader & JNI Utilities for Java

---

## License

MIT License. See [LICENSE](LICENSE) file for details.

---

**Part of the FastJava Ecosystem** — *Making the JVM faster.* 🚀