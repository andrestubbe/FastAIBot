# FastBot 0.1.0 [ALPHA-2026-06] — High-Performance Bot Orchestrator for Java

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![Platform](https://img.shields.io/badge/Platform-Windows%2010+-lightgrey.svg)]()
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)]()

**⚡ A zero-latency, asynchronous orchestration runtime connecting LLM brains, conversation memory, and multi-channel outputs.**

FastBot is the central nervous system of the **FastJava** ecosystem. It bridges the pure AI generation of `FastAI` with persistent state (`FastAIMemory`) and high-performance interactive interfaces (`FastTerminal`, `FastTTS`, `FastFace`).

By natively parsing LLM output streams in real-time, FastBot allows for instantaneous multi-channel dispatch—enabling bots that can talk, type, and animate their faces simultaneously without JSON-parsing latency.

---

## Quick Start — Example

```java
import fastbot.FastBot;
import fastai.FastAI;
import fastai.AI;

public class Demo {
    public static void main(String[] args) {
        // 1. Connect the Brain
        AI brain = FastAI.connect("gemini:gemini-1.5-flash", "api-key");

        // 2. Define Multi-Channel Output (The Mixer)
        Consumer<String> textOut = text -> System.out.print(text); // To FastTerminal/TTS
        Consumer<String> actionOut = action -> triggerFaceAnimation(action); // To 3D Engine

        // 3. Boot the Bot
        FastBot bot = new FastBot(brain, "You are a sarcastic AI...", textOut, actionOut);

        // 4. Talk (Streams instantly, bypassing JSON lag)
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
The standard approach to multi-modal AI involves parsing massive JSON blocks, resulting in slow responsiveness. The FastJava philosophy requires zero latency. FastBot uses an inline-tag stream parser (`[ACTION:...]`) to instantly split text and behavior, giving bots immediate responsiveness while maintaining a pure, stateless connection layer in `FastAI`.

---

## Key Features
* **🚫 Zero Lag Routing** — Parses tokens directly as they arrive, avoiding blocking JSON parsers.
* **🎭 Multi-Channel Mixer** — Splits speech and animations into separate execution pipelines.
* **🧠 Context Awareness** — Seamlessly integrates with `FastAIMemory` for session persistence.
* **⚡ State-Minimized** — Built entirely in pure Java with zero heavy framework bloat.

---

## Architecture Overview (FastBot vs FastAI)

**FastAI (The Brain)**  
Minimalistischer, hyper-schneller Java-LLM-Client.
- Keine Abhängigkeiten.
- Kein Event-System.
- Kein Tool-Routing.
- Kein State-Management.
- Kein Output-Mixing.
→ *Nur: Prompt rein, Tokens raus.*

**FastBot (The Nervous System)**  
Die Orchestrator-Runtime für Bots, Agenten und interaktive Systeme.
Verbindet `FastAI`, `FastAIMemory`, `FastAIModel`, `FastTerminal`, `FastTTS` und Tools.
- **FastBotSessionManager**: Verwaltet Memory-Scopes und Context-IDs.
- **FastBotEventLoop**: Non-blocking Dispatcher für parallele Streams.
- **FastBotPipelineEngine**: Baut Prompts, ruft AI auf, leitet Ausgaben weiter.
- **FastBotToolBridge**: Registriert und führt Java-Methoden als LLM-Tools aus.
- **FastBotOutputMixer**: Multi-Channel Output (Terminal, Audio, Face).
- **FastBotStateMachine**: Bot-Zustände (Interview Mode, Silent Mode).
- **FastBotTelemetry**: Token-, Event- und Tool-Logs.

---

## API Quick Reference

| Method | Description | Path |
|--------|-------------|------|
| `streamChat(String)` | Sends input to FastAI and streams the mixed output. | [Reference →](docs/REFERENCE.md#streamchat) |
| `getHistory()` | Returns the active `ConversationHistory`. | [Reference →](docs/REFERENCE.md#gethistory) |

> [!TIP]
> See **[docs/REFERENCE.md](docs/REFERENCE.md)** for full documentation on Action Tags and Output Routing.

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
* **[docs/REFERENCE.md](docs/REFERENCE.md)**: Full API contracts and routing logic.
* **[docs/PHILOSOPHY.md](docs/PHILOSOPHY.md)**: The "Zero Latency" orchestrator philosophy.
* **[docs/COMPILE.md](docs/COMPILE.md)**: Build instructions.
* **[docs/CHANGELOG.md](docs/CHANGELOG.md)**: Project history.
* **[docs/ROADMAP.md](docs/ROADMAP.md)**: Future development goals.

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
