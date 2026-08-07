# FastAIBot Reference

This document outlines the API contracts and architectural patterns for the `FastAIBot` orchestration runtime.

## Core Class

### `FastAIBot`
The central bot orchestrator connecting `FastAI` inference with `FastAIMemory` history and `FastString` zero-allocation response buffering.

#### Constructors
- `public FastAIBot(AI ai, String systemPrompt, Consumer<String> textOutput)`  
  Initializes the orchestrator with default `PlainTextFormatter` and default 64 KB native `FastString` buffer.

- `public FastAIBot(AI ai, String systemPrompt, Consumer<String> textOutput, MemoryFormatter formatter)`  
  Initializes the orchestrator with a custom memory formatter.

- `public FastAIBot(AI ai, String systemPrompt, Consumer<String> textOutput, MemoryFormatter formatter, int initialBufferBytes)`  
  Initializes the orchestrator with custom memory formatting and configurable initial native `FastString` buffer capacity.

#### Methods
- `public void streamChat(String userInput)`  
  Executes a multi-turn chat interaction. Appends user input to `ConversationHistory`, streams response tokens real-time to `textOutput`, and buffers the full response in native memory without Java heap allocation.

- `public ConversationHistory getHistory()`  
  Returns the active conversation history instance.
