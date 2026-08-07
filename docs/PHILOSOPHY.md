# The Philosophy of FastAIBot

> [!IMPORTANT]
> **"Zero Latency. Zero GC Overhead. Instant Token Streaming."**

FastAIBot operates on a fundamental principle of the FastJava ecosystem: conversational AI must stream tokens to the user the exact millisecond they arrive from the LLM, without heavy JSON parsing or object allocation overhead.

### Core Tenets

1. **Direct Token Streaming**
   FastAIBot pipes streaming LLM tokens straight to the designated consumer as they arrive, providing zero-latency real-time response generation.

2. **Native Zero-Allocation Buffering**
   By leveraging `FastString` off-heap native memory buffers, FastAIBot records the full assistant response into `FastAIMemory` without creating temporary Heap objects or causing Garbage Collector pauses.

3. **Absolute Separation of Concerns**
   - **FastAI**: Generates LLM tokens.
   - **FastAIMemory**: Manages multi-turn conversation history and context formatting.
   - **FastAIBot**: Orchestrates LLM execution with persistent state and streaming output channels.

4. **No Heavy Frameworks**
   FastAIBot is built with zero framework overhead. It contains no dependency injection containers or heavy abstractions—just fast, predictable Java execution.
