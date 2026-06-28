# FastBot 0.1.0 [ALPHA] — The High-Performance Bot Orchestrator

[![Status](https://img.shields.io/badge/status-0.1.0-brightgreen.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.java.com)
[![JitPack](https://img.shields.io/badge/JitPack-ready-green.svg)]()

**⚡ The "Body + Nervous System" for autonomous bots and agents.**

FastBot is the orchestration runtime of the **FastJava** ecosystem. It connects your LLM Client (`FastAI`), your Conversation Context (`FastAIMemory`), and your interfaces (`FastTerminal`, `FastTTS`, `FastFace`) into a single, cohesive, ultra-low-latency event loop.

---

## FastAI vs FastBot (Architecture Overview)

**FastAI**  
Minimalistischer, hyper-schneller Java-LLM-Client.
- Keine Abhängigkeiten.
- Kein Event-System.
- Kein Tool-Routing.
- Kein State-Management.
- Kein Output-Mixing.
→ *Nur: Prompt rein, Tokens raus.*

**FastBot**  
Die Orchestrator-Runtime für Bots, Agenten und interaktive Systeme.
Verbindet `FastAI`, `FastAIMemory`, `FastAIModel`, `FastTerminal`, `FastTTS` und Tools.
Beinhaltet:
- **FastBotSessionManager**: Verwaltet Memory-Scopes und Context-IDs.
- **FastBotEventLoop**: Non-blocking Dispatcher für parallele Streams.
- **FastBotPipelineEngine**: Baut Prompts, ruft AI auf, leitet Ausgaben weiter.
- **FastBotToolBridge**: Registriert und führt Java-Methoden als LLM-Tools aus.
- **FastBotOutputMixer**: Multi-Channel Output (Terminal, Audio, Face).
- **FastBotStateMachine**: Bot-Zustände (Interview Mode, Silent Mode).
- **FastBotTelemetry**: Token-, Event- und Tool-Logs.

→ *FastBot ist der „Körper + das Nervensystem“ eines Bots.*  
→ *FastAI bleibt das „Gehirn“.*

---

## Quick Start (Minimal Demo)

```java
// 1. Connect the Brain
AI brain = FastAI.connect("openai:gpt-4o", apiKey);

// 2. Define Output Channels (The Mixer)
Consumer<String> terminalOutput = text -> System.out.print(text);
Consumer<String> faceAnimator = action -> trigger3DHead(action);

// 3. Boot the Bot
FastBot bot = new FastBot(brain, "You are a sarcastic LinkedIn Bot...", terminalOutput, faceAnimator);

// 4. Talk
bot.streamChat("Are you a bot in a monitor?");
```

---

## Core Modules

### 🎛️ FastBotOutputMixer
Instead of waiting for slow JSON responses, FastBot uses **Inline Action Tags** (`[ACTION:look_down]`) parsed directly from the token stream. 
This means text flows to `FastTTS` at zero latency, while the `[ACTION]` triggers the 3D Face Animation exactly at the right millisecond. No buffering. No lag.

### 🧠 FastBotSessionManager (TODO)
Routes tokens to the correct `FastAIMemory` instance, keeping user conversations isolated without crossing memory boundaries.

### 🔌 FastBotToolBridge (TODO)
Bind standard Java methods to LLM tool calls. `FastAI` emits the tool request, `FastBot` validates it and executes the Java code cleanly.

---

## Installation

Add the JitPack repository and the dependency to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fastbot</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

---
**Part of the FastJava Ecosystem** — *Making the JVM faster. Small package. Maximum speed. Zero bloat. 🚀📋*
