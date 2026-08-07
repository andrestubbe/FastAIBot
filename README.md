# FastBot 0.1.1 [ALPHA-2026-08] — High-Performance Bot Orchestrator for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)]()

**⚡ A zero-latency, asynchronous orchestration runtime connecting LLM brains and conversation memory.**

FastBot is the orchestrator of the **FastJava** ecosystem. It bridges the pure AI generation of `FastAI` with persistent state (`FastAIMemory`) and streaming interfaces.

By streaming LLM output tokens in real-time, FastBot enables instant responses without JSON-parsing latency.

---

## Quick Start — Example

```java
import fastaibot.FastAIBot;
import fastai.FastAI;
import fastai.AI;
import java.util.function.Consumer;

public class Demo {
    public static void main(String[] args) {
        // 1. Connect the Brain
        AI brain = FastAI.connect("gemini:gemini-1.5-flash", "api-key");

        // 2. Define Text Output Consumer
        Consumer<String> textOut = text -> System.out.print(text);

        // 3. Boot the Bot
        FastAIBot bot = new FastAIBot(brain, "You are a helpful AI...", textOut);

        // 4. Talk (Streams instantly)
        bot.streamChat("Are you a bot in a monitor?");
    }
}
```

---

## Table of Contents
- [Why FastBot?](#why-fastbot)
- [Key Features](#key-features)
- [Architecture Overview (FastBot vs FastAI)](#architecture-overview-fastbot-vs-fastai)
- [API Quick Reference](#api-quick-reference)
- [Installation](#installation)
- [Documentation](#documentation)
- [Platform Support](#platform-support)
- [License](#license)

---

## Why FastBot?
Traditional AI frameworks add heavy abstraction layers and slow JSON parsing overhead, introducing noticeable latency to live interactions. FastBot provides a zero-latency, high-performance orchestration layer for Java. It connects `FastAI`'s stateless LLM stream directly with `FastAIMemory`'s context history, streaming incoming response tokens to your application in real-time.

---

## Key Features
* **🚫 Zero Lag Streaming** — Passes tokens directly as they arrive without JSON-parsing overhead.
* **🧠 Context Awareness** — Seamlessly integrates with `FastAIMemory` for session persistence.
* **⚡ State-Minimized** — Built entirely in pure Java with zero heavy framework bloat.

---

## Architecture Overview

**FastAI (The Brain)**  
Minimalistischer, hyper-schneller Java-LLM-Client.
- Keine Abhängigkeiten.
- Kein Event-System.
- Kein State-Management.
→ *Nur: Prompt rein, Tokens raus.*

**FastAIMemory (The Memory)**  
Modul zur Verwaltung von Gesprächskontexten und Verläufen.
- **ConversationHistory**: Speichert und strukturiert Dialoghistorien (System, User, Assistant).
- **MemoryContextBuilder**: Baut formatierte Prompts unter Einbeziehung des Chat-Kontextes.

**FastAIBot (The Orchestrator)**  
Die schlanke Orchestrator-Runtime für interaktive Bot-Systeme.
- Verbindet `FastAI` (LLM-Inferenz) und `FastAIMemory` (Kontext-Verwaltung).
- Steuert den Echtzeit-Chatverlauf und verteilt Token-Streams ohne Overhead.

---

## API Quick Reference

| Method | Description | Path |
|--------|-------------|------|
| `streamChat(String)` | Sends input to FastAI and streams the mixed output. | [Reference →](docs/REFERENCE.md#streamchat) |
| `getHistory()` | Returns the active `ConversationHistory`. | [Reference →](docs/REFERENCE.md#gethistory) |

> [!TIP]
> See **[REFERENCE.md](docs/REFERENCE.md)** for full documentation.

---

## Installation

### Maven (JitPack)
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastbot</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

---

## Documentation
* **[REFERENCE.md](docs/REFERENCE.md)**: Full API contracts and routing logic.
* **[PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The "Zero Latency" orchestrator philosophy.
* **[COMPILE.md](docs/COMPILE.md)**: Build instructions.
* **[CHANGELOG.md](docs/CHANGELOG.md)**: Project history.
* **[ROADMAP.md](docs/ROADMAP.md)**: Future development goals.

---

## Platform Support
| Platform | Status |
|----------|--------|
| Windows 10/11 (x64) | ✅ Fully Supported |
| Linux | ✅ Fully Supported |
| macOS | ✅ Fully Supported |

---

## License
MIT License — See [LICENSE](LICENSE) file for details.

---

## Related Projects
- [FastAI](https://github.com/andrestubbe/FastAI)
- [FastAIMemory](https://github.com/andrestubbe/FastAIMemory)
- [FastTerminal](https://github.com/andrestubbe/FastTerminal)

---

**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*
