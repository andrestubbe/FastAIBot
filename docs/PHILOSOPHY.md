# The Philosophy of FastBot

> [!IMPORTANT]
> **"Zero JSON latency. Parallel Output. Pure Execution."**

FastBot operates on a fundamental truth of the FastJava ecosystem: parsing massive JSON blobs to separate a bot's actions from its speech is fundamentally too slow for real-time interaction. 

### Core Tenets

1. **Inline Streaming Logic**
   By forcing the LLM to output actions inline (`[ACTION:...]`), FastBot can act upon those commands the exact millisecond they stream in, simultaneously piping the raw text to audio generation (`FastTTS`) or terminal rendering (`FastTerminal`).

2. **Absolute Separation of Concerns**
   - **FastAI** generates tokens. It does not know what those tokens do.
   - **FastBot** orchestrates the tokens. It connects them to tools, UI, and memory.

3. **No Heavy Frameworks**
   FastBot does not use Spring Contexts, dependency injection, or bloated abstractions. It is pure, instantiated Java designed for absolute speed and minimal garbage collection.
